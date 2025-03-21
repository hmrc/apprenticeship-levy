/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apprenticeshiplevy.connectors

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.apprenticeshiplevy.data.des.EmploymentCheckStatus._
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.metrics._
import uk.gov.hmrc.apprenticeshiplevy.utils.{ClosedDateRange, DateRange}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html.helper

import java.net.URLDecoder
import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait EmployerDetailsEndpoint extends Timer with Logging {
  des: DesConnector =>

  def designatoryDetails(empref: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesignatoryDetails] = {
    val emprefParts = "^(\\d{3})([^0-9A-Z]*)([0-9A-Z]{1,10})$".r

    val (office, ref) =
      URLDecoder.decode(empref, "UTF-8") match {
        case emprefParts(part1, _, part2) =>
          (part1, part2)
        case _ =>
          throw new IllegalArgumentException(s"Empref is not valid.")
    }

    val url = s"${des.baseUrl}/paye/employer/$office/$ref/designatory-details"

    // $COVERAGE-OFF$
    logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    val details: DesignatoryDetails =
      DesignatoryDetails(Some(empref))

    timer(RequestEvent(DES_EMPREF_DETAILS_REQUEST, Some(empref))) {
      audit(ALAEvent("readEmprefDetails", empref)) {
        createDesHeaders(des.httpClient.get(url"$url"))
          .execute[Either[UpstreamErrorResponse, HodDesignatoryDetailsLinks]] flatMap {
          case Right(response) =>
            response.links.map {
              links =>
                val employer: Future[Option[DesignatoryDetailsData]] =
                  links
                    .employer
                    .map(getDetails(_))
                    .getOrElse(Future.successful(None))
                val communication: Future[Option[DesignatoryDetailsData]] =
                  links
                    .communication
                    .map(getDetails(_))
                    .getOrElse(Future.successful(None))
                employer.flatMap {
                  emp =>
                    communication.flatMap {
                      com =>
                        Future.successful(details.copy(employer = emp, communication = com))
                    }
                }.recoverWith {
                  case e: Throwable =>
                    logger.error(s"getDetails call failed: ${e.getMessage}")
                    Future.successful(details)
                }
            }.getOrElse(Future.successful(details))
          case Left(e: UpstreamErrorResponse) =>
            if (e.statusCode == BAD_REQUEST)
              throw new BadRequestException(e.message)
            if (e.statusCode == NOT_FOUND)
              throw new NotFoundException(e.message)
            else
              throw UpstreamErrorResponse(e.message, e.statusCode, e.reportAs)
        }
      }
    }
  }

  private def getDetails(path: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DesignatoryDetailsData]] = {
    val url = s"${des.baseUrl}$path"
    createDesHeaders(des.httpClient.get(url"$url"))
      .execute[Either[UpstreamErrorResponse, DesignatoryDetailsData]] map {
      case Right(data) =>
        Some(data)
      case Left(e: UpstreamErrorResponse) =>
        if (e.statusCode == BAD_REQUEST)
          throw new BadRequestException(e.message)
        if (e.statusCode == NOT_FOUND)
          throw new NotFoundException(e.message)
        else
          // $COVERAGE-OFF$
          logger.warn(s"Unable to get designatory details. HTTP STATUS ${e.getMessage}. Returning NONE", e)
        // $COVERAGE-ON$
          None
        }
  }
}

trait EmploymentCheckEndpoint extends Timer {
  des: DesConnector =>

  def check(empref: String, nino: String, dateRange: ClosedDateRange)
           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmploymentCheckStatus] = {
    val dateParams = dateRange.toParams
    val url = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/employed/${helper.urlEncode(nino)}?$dateParams"

    // $COVERAGE-OFF$
    logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_EMP_CHECK_REQUEST, Some(empref))) {
      audit(ALAEvent("employmentCheck", empref, nino, dateParams)) {
        createDesHeaders(des.httpClient.get(url"$url"))
          .execute[Either[UpstreamErrorResponse, EmploymentCheckStatus]] map {
          case Right(response) =>
            response
          case Left(e: UpstreamErrorResponse) =>
            if (e.statusCode == BAD_REQUEST)
              throw new BadRequestException(e.message)
            if (e.statusCode == NOT_FOUND)
              Unknown
            else
              throw UpstreamErrorResponse(e.message, e.statusCode, e.reportAs)
        }
      }
    }
  }
}

trait FractionsEndpoint extends Timer {
  des: DesConnector =>

  def fractions(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Fractions] = {
    val dateParams = dateRange.toParams
    val url = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/fractions?$dateParams"
    // $COVERAGE-OFF$
    logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_FRACTIONS_REQUEST, Some(empref))) {
      audit(ALAEvent("readFractions", empref, "", dateParams)) {
        createDesHeaders(des.httpClient.get(url"$url"))
          .execute[Either[UpstreamErrorResponse, Fractions]] map {
          case Right(fraction) =>
            fraction.copy(empref = convertEmpref(fraction.empref))
          case Left(e: UpstreamErrorResponse) =>
            if (e.statusCode == BAD_REQUEST)
              throw new BadRequestException(e.message)
            if (e.statusCode == NOT_FOUND)
              throw new NotFoundException(e.message)
            else
              throw UpstreamErrorResponse(e.message, e.statusCode, e.reportAs)
        }
      }
    }
  }

  def fractionCalculationDate(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    val url = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"

    // $COVERAGE-OFF$
    logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_FRACTIONS_DATE_REQUEST, None)) {
      audit(ALAEvent("readFractionCalculationDate")) {
        createDesHeaders(des.httpClient.get(url"$url"))
          .execute[Either[UpstreamErrorResponse, FractionCalculationDate]] map {
          case Right(response) =>
            response.date
          case Left(e: UpstreamErrorResponse) =>
            if (e.statusCode == BAD_REQUEST)
              throw new BadRequestException(e.message)
            if (e.statusCode == NOT_FOUND)
              throw new NotFoundException(e.message)
            else
              throw UpstreamErrorResponse(e.message, e.statusCode, e.reportAs)
        }
      }
    }
  }
}

trait LevyDeclarationsEndpoint extends Timer {
  des: DesConnector =>

  def eps(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmployerPaymentsSummary] = {
    val dateParams = dateRange.toParams
    val url = s"${desURL(empref)}?$dateParams"

    timer(RequestEvent(DES_LEVIES_REQUEST, Some(empref))) {
      audit(ALAEvent("readLevyDeclarations", empref, "", dateParams)) {
        createDesHeaders(des.httpClient.get(url"$url"))
          .execute[Either[UpstreamErrorResponse, HttpResponse]] map {
          case Right(response) =>
            marshall(empref, response.body).getOrElse {
              logger.error(s"""|DES url $url
                               |HTTP status 200 returned but json was not EPS or EPS error. Actual response is:
                               |Status: ${response.status}
                               |Headers: ${response.headers.mkString(" ")}
                               |Body: '${response.body}'""".stripMargin('|'))
              throw new IllegalArgumentException(s"DES returned unexpected JSON on 200 response: ${response.body}")
            }
          case Left(e: UpstreamErrorResponse) =>
            if (e.statusCode == BAD_REQUEST)
              throw new BadRequestException(e.message)
            if (e.statusCode == NOT_FOUND)
              throw new NotFoundException(e.message)
            else
              throw UpstreamErrorResponse(e.message, e.statusCode, e.reportAs)
        }
      }
    }
  }

  protected[connectors] def marshall(empref: String, jsonStr: String): Option[EmployerPaymentsSummary] =
    Seq(
      "EmployerPaymentsSummary",
      "EmployerPaymentsSummaryVersion0",
      "EmployerPaymentsError",
      "EmptyEmployerPayments"
    )
      .find(toEmployerPaymentsSummary(_, jsonStr).isDefined)
      .flatMap(toEmployerPaymentsSummary(_, jsonStr))
      .map(
        eps =>
          if (eps.empref.isEmpty)
            eps.copy(empref = convertEmpref(empref))
          else
            eps.copy(empref = convertEmpref(eps.empref))
      )

  private def toEmployerPaymentsSummary(className: String, jsonStr: String): Option[EmployerPaymentsSummary] = {
    def toEPSResponse[A <: EPSResponse](jsResult: JsResult[A]): Option[EPSResponse] =
      jsResult match {
        case JsSuccess(response, _) =>
          Some[EPSResponse](response)
        case JsError(_) =>
          None
      }

    def maybeEPSResponse(className: String, jsonStr: String): Option[EPSResponse] = {
      // As noted above this feels likes generics and ClassTag should be used but hotfix required
      className match {
        case "EmptyEmployerPayments" => Try(Json.parse(jsonStr).validate[EmptyEmployerPayments]).toOption.flatMap(toEPSResponse(_))
        case "EmployerPaymentsSummary" => Try(Json.parse(jsonStr).validate[EmployerPaymentsSummary]).toOption.flatMap(toEPSResponse(_))
        case "EmployerPaymentsSummaryVersion0" => Try(Json.parse(jsonStr).validate[EmployerPaymentsSummaryVersion0]).toOption.flatMap(toEPSResponse(_))
        case "EmployerPaymentsError" => Try(Json.parse(jsonStr).validate[EmployerPaymentsError]).toOption.flatMap(toEPSResponse(_))
        case _ => throw new IllegalArgumentException(("!"*10) + " PROGRAMMING ERROR: Fix className match above")
      }
    }

    maybeEPSResponse(className, jsonStr) match {
      case Some(EmployerPaymentsSummaryVersion0(empref, eps)) =>
        Some(EmployerPaymentsSummary(empref, eps))
      case Some(EmployerPaymentsError(reason)) =>
        logger.error(s"DES reported error reason '$reason' on HTTP 200 response.")
        throw UpstreamErrorResponse.apply(s"DES returned error code object on HTTP 200 response (treating as error). DES error reason: '$reason'.", PRECONDITION_FAILED)
      case Some(EmptyEmployerPayments(empref)) =>
        Some(EmployerPaymentsSummary(empref, List.empty[EmployerPaymentSummary]))
      case Some(EmployerPaymentsSummary(empref, eps)) =>
        Some(EmployerPaymentsSummary(empref, eps))
      case Some(_) =>
        logger.error(s"Got Some(_) unknown type - unclear how to handle")
        None
      case None =>
        val isEmpty = "^\\s*(\\{\\s*})\\s*$".r
        isEmpty findFirstIn jsonStr map (_ => EmployerPaymentsSummary("", List.empty[EmployerPaymentSummary]))
    }
  }

  private def isEpsOrigPathEnabled: Boolean = appContext.epsOrigPathEnabled()

  private def desURL(empref: String): String = if (isEpsOrigPathEnabled)
                                                          s"$baseUrl/rti/employers/${helper.urlEncode(empref)}/employer-payment-summary"
                                                        else
                                                          s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/declarations"
}

trait DesConnector extends FractionsEndpoint
  with EmployerDetailsEndpoint
  with EmploymentCheckEndpoint
  with LevyDeclarationsEndpoint
  with Auditor
  with GraphiteMetrics {
  def httpClient: HttpClientV2
  def baseUrl: String
  def desAuthorization: String
  def desEnvironment: String

  private val EMPREF = "([0-9]{3})([\\/]*)([a-zA-Z0-9]+)".r

  def convertEmpref(empref: String): String =
    empref match {
      case EMPREF(taxOffice,_,ref) => s"$taxOffice/$ref"
      case _ => empref
    }

  def createDesHeaders(builder: RequestBuilder)(implicit hc: HeaderCarrier): RequestBuilder = {
    builder
      .setHeader("X-Client-ID" -> getHeaderValueByKey("X-Client-ID"))
      .setHeader("Authorization" -> s"Bearer $desAuthorization")
      .setHeader("Environment" -> desEnvironment)
      .setHeader("CorrelationId" -> UUID.randomUUID().toString)
  }

  private def getHeaderValueByKey(key: String)(implicit headerCarrier: HeaderCarrier): String = {
    headerCarrier.headers(Seq(key)).toMap.getOrElse(key, "")
  }
}

class LiveDesConnector @Inject()(val httpClient: HttpClientV2,
                                 getAuditConnector: AuditConnector,
                                 val appContext: AppContext,
                                 metrics: MetricRegistry) extends DesConnector{
  protected def auditConnector: Option[AuditConnector] = Some(getAuditConnector)
  def baseUrl: String = appContext.desUrl

  override def registry: Option[MetricRegistry] = if (appContext.metricsEnabled) Try (Some(metrics)).getOrElse(None) else None

  override def desAuthorization: String = appContext.desToken

  override def desEnvironment: String = appContext.desEnvironment
}

class SandboxDesConnector @Inject()(val httpClient: HttpClientV2,
                                    val appContext: AppContext,
                                    metrics: MetricRegistry) extends DesConnector{
  protected def auditConnector: Option[AuditConnector] = None
  def baseUrl: String = appContext.stubDesUrl

  override def registry: Option[MetricRegistry] = if (appContext.metricsEnabled) Try (Some(metrics)).getOrElse(None) else None

  override def desAuthorization: String = appContext.desToken

  override def desEnvironment: String = appContext.desEnvironment
}
