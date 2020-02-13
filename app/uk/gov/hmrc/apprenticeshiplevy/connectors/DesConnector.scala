/*
 * Copyright 2020 HM Revenue & Customs
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

import java.net.URLDecoder

import com.google.inject.Inject
import org.joda.time.LocalDate
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, MicroserviceAuditFilter}
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.apprenticeshiplevy.data.des.DesignatoryDetails._
import uk.gov.hmrc.apprenticeshiplevy.data.des.EmploymentCheckStatus._
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.metrics._
import uk.gov.hmrc.apprenticeshiplevy.utils.{ClosedDateRange, DateRange}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import views.html.helper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DesUtils {
  val EMPREF = "([0-9]{3})([\\/]*)([a-zA-Z0-9]+)".r

  def convertEmpref(empref: String): String =
    empref match {
      case EMPREF(taxOffice,_,ref) => s"$taxOffice/$ref"
      case _ => empref
    }
}

trait EmployerDetailsEndpoint extends Timer {
  des: DesConnector =>

  def designatoryDetails(empref: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesignatoryDetails] = {
    val emprefParts = "^(\\d{3})([^0-9A-Z]*)([0-9A-Z]{1,10})$".r
    val (office, ref) = URLDecoder.decode(empref, "UTF-8") match {
      case emprefParts(part1, _, part2) => (part1, part2)
      case _ => throw new IllegalArgumentException(s"Empref is not valid.")
    }

    val url = s"${des.baseUrl}/paye/employer/${office}/${ref}/designatory-details"

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_EMPREF_DETAILS_REQUEST, Some(empref))) {
      audit(new ALAEvent("readEmprefDetails", empref)) {
        des.httpGet.GET[HodDesignatoryDetailsLinks](url).flatMap { response =>
          val details = DesignatoryDetails(Some(empref))
          response.links.map { links =>
            Logger.debug((links.employer ++ links.communication).mkString(" "))
            val getEmployer = links.employer.map(getDetails(_)).getOrElse(Future.successful(None))
            val getComms = links.communication.map(getDetails(_)).getOrElse(Future.successful(None))
            for {
              e <- getEmployer
              c <- getComms
            } yield (details.copy(employer=e, communication=c))
          }.getOrElse(Future.successful(details))
        }(ec)
      }
    }
  }

  protected def getDetails(path: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DesignatoryDetailsData]] =
    des.httpGet.GET[DesignatoryDetailsData](s"${des.baseUrl}${path}").map { data => Some(data) }.recover(errorHandler)

  protected val errorHandler: PartialFunction[Throwable, Option[DesignatoryDetailsData]] = {
        case e => {
          // $COVERAGE-OFF$
          Logger.warn(s"Unable to get designatory details. HTTP STATUS ${e.getMessage}. Returning NONE", e)
          // $COVERAGE-ON$
          None
        }
    }
}

trait EmploymentCheckEndpoint extends Timer {
  des: DesConnector =>

  def check(empref: String, nino: String, dateRange: ClosedDateRange)
           (implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext): Future[EmploymentCheckStatus] = {
    val dateParams = dateRange.toParams.getOrElse("")
    val url = s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/employed/${helper.urlEncode(nino)}?${dateParams}"

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_EMP_CHECK_REQUEST, Some(empref))) {
      audit(ALAEvent("employmentCheck", empref, nino, dateParams)) {
        des.httpGet.GET[EmploymentCheckStatus](url).recover { case _: NotFoundException => Unknown }
      }
    }
  }
}

trait FractionsEndpoint extends Timer with DesUtils {
  des: DesConnector =>

  def fractions(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Fractions] = {
    val dateParams = dateRange.toParams.getOrElse("")
    val url = (s"$baseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/fractions", dateRange.toParams) match {
      case (u, Some(params)) => s"$u?$params"
      case (u, None) => u
    }

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_FRACTIONS_REQUEST, Some(empref))) {
      audit(new ALAEvent("readFractions", empref, "", dateParams)) {
        des.httpGet.GET[Fractions](url).map { fraction =>
          fraction.copy(empref=convertEmpref(fraction.empref))
        }
      }
    }
  }

  def fractionCalculationDate(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[LocalDate] = {
    val url = s"$baseUrl/apprenticeship-levy/fraction-calculation-date"

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_FRACTIONS_DATE_REQUEST, None)) {
      audit(new ALAEvent("readFractionCalculationDate")) {
        des.httpGet.GET[FractionCalculationDate](url).map { _.date }
      }
    }
  }
}

trait LevyDeclarationsEndpoint extends Timer with DesUtils {
  des: DesConnector =>

  def eps(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmployerPaymentsSummary] = {
    val dateParams = dateRange.toParams.getOrElse("")

    val url = s"${desURL(empref)}${params(dateRange)}"

    timer(RequestEvent(DES_LEVIES_REQUEST, Some(empref))) {
      audit(new ALAEvent("readLevyDeclarations", empref, "", dateParams)) {
        des.httpGet.GET[HttpResponse](url).map { response =>
          marshall(empref, response.body).getOrElse {
            Logger.error(s""" |DES url ${url}
                              |HTTP status 200 returned but json was not EPS or EPS error. Actual response is:
                              |  Status: ${response.status}
                              |  Headers: ${response.allHeaders.mkString(" ")}
                              |  Body: '${response.body}'""".stripMargin('|'))
            throw new IllegalArgumentException(s"DES returned unexpected JSON on 200 response: ${response.body}")
          }
        }
      }
    }
  }

  protected[connectors] def marshall(empref: String, jsonStr: String): Option[EmployerPaymentsSummary] = {
    // This feels like generics and ClassTag should be used but hotfix for bad DES response required TODO
    val responseTypes = Seq("EmployerPaymentsSummary", "EmployerPaymentsSummaryVersion0", "EmployerPaymentsError", "EmptyEmployerPayments")
    responseTypes
      .find(toEmployerPaymentsSummary(_, jsonStr).isDefined)
      .map(toEmployerPaymentsSummary(_, jsonStr))
      .head
      .map(eps=> if (eps.empref.isEmpty) eps.copy(empref=convertEmpref(empref)) else eps.copy(empref=convertEmpref(eps.empref)))
  }

  protected[connectors] def toEmployerPaymentsSummary(className: String, jsonStr: String): Option[EmployerPaymentsSummary] = {
    import uk.gov.hmrc.apprenticeshiplevy.data.des.EmployerPaymentsError._
    import uk.gov.hmrc.apprenticeshiplevy.data.des.EmployerPaymentsSummary._
    import uk.gov.hmrc.apprenticeshiplevy.data.des.EmployerPaymentsSummaryVersion0._
    import uk.gov.hmrc.apprenticeshiplevy.data.des.EmptyEmployerPayments._

    def toEPSResponse[A <: EPSResponse](jsResult: JsResult[A]): Option[EPSResponse] = jsResult match {
      case JsSuccess(response,_) => Some[EPSResponse](response)
      case JsError(_) => None
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
      case Some(EmployerPaymentsError(reason)) => {
        Logger.error(s"DES reported error reason '${reason}' on HTTP 200 response.")
        throw new Upstream5xxResponse(s"DES returned error code object on HTTP 200 response (treating as error). DES error reason: '${reason}'.", PRECONDITION_FAILED, PRECONDITION_FAILED)
      }
      case Some(EmptyEmployerPayments(empref)) =>
        Some(EmployerPaymentsSummary(empref, List.empty[EmployerPaymentSummary]))
      case Some(EmployerPaymentsSummary(empref,eps)) =>
        Some(EmployerPaymentsSummary(empref, eps))
      case Some(_) => {
        Logger.error(s"Got Some(_) unknown type - unclear how to handle")
        None
      }
      case None => {
        val isEmpty = "^\\s*(\\{\\s*\\})\\s*$".r
        isEmpty findFirstIn jsonStr map(_ => EmployerPaymentsSummary("", List.empty[EmployerPaymentSummary]))
      }
    }
  }

  protected[connectors] def isEpsOrigPathEnabled: Boolean = AppContext.epsOrigPathEnabled

  protected[connectors] def params(dateRange: DateRange): String = dateRange.toParams match {
      case None => {
        val defaultRange = ClosedDateRange(new LocalDate().minusYears(AppContext.defaultNumberOfDeclarationYears), new LocalDate())
        defaultRange.toParams.map(p=>s"?${p}").getOrElse("")
      }
      case Some(ps) => s"?${ps}"
    }

  protected[connectors] def desURL(empref: String): String = if (isEpsOrigPathEnabled)
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
  def httpGet: HttpGet
  def baseUrl: String
}

class LiveDesConnector @Inject()(val httpGet: HttpGet) extends DesConnector {
  protected def auditConnector: Option[AuditConnector] = Some(MicroserviceAuditFilter.auditConnector)
  def baseUrl: String = AppContext.desUrl
}

class SandboxDesConnector @Inject()(val httpGet: HttpGet) extends DesConnector {
  protected def auditConnector: Option[AuditConnector] = None
  def baseUrl: String = AppContext.stubDesUrl
}
