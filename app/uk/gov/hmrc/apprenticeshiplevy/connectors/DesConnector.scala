/*
 * Copyright 2016 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.apprenticeshiplevy.utils.{DateRange, ClosedDateRange}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, NotFoundException}
import views.html.helper
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import scala.concurrent.{Future, ExecutionContext}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.config.MicroserviceAuditFilter
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.metrics._

trait DesUrl {
  def baseUrl: String
}

trait DesSandboxUrl extends DesUrl {
  def baseUrl: String = AppContext.stubDesUrl
}

trait DesProductionUrl extends DesUrl {
  def baseUrl: String = AppContext.desUrl
}

trait EmployerDetailsEndpoint extends Timer {
  des: DesConnector =>

  def designatoryDetails(empref: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesignatoryDetails] = {

    val url = s"${des.baseUrl}/epaye/${helper.urlEncode(empref)}/designatory-details"

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_EMPREF_DETAILS_REQUEST, Some(empref))) {
      audit(new ALAEvent("readEmprefDetails", empref)) {
        des.httpGet.GET[DesignatoryDetails](url).map(_.copy(empref = Some(empref)))(ec)
      }
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
      audit(new ALAEvent("employmentCheck", empref, nino, dateParams)) {
        des.httpGet.GET[EmploymentCheckStatus](url).recover { case notFound: NotFoundException => NinoUnknown }
      }
    }
  }
}

trait FractionsEndpoint extends Timer {
  des: DesConnector =>

  def fractions(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext): Future[Fractions] = {
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
        des.httpGet.GET[Fractions](url)
      }
    }
  }

  def fractionCalculationDate(implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext): Future[LocalDate] = {
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

trait LevyDeclarationsEndpoint extends Timer {
  des: DesConnector =>

  def eps(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier, ec: scala.concurrent.ExecutionContext): Future[EmployerPaymentsSummary] = {
    val dateParams = dateRange.toParams.getOrElse("")
    val url = (s"$baseUrl/rti/employers/${helper.urlEncode(empref)}/employer-payment-summary", dateRange.toParams) match {
      case (u, None) => u
      case (u, Some(ps)) => s"$u?$ps"
    }

    // $COVERAGE-OFF$
    Logger.debug(s"Calling DES at $url")
    // $COVERAGE-ON$

    timer(RequestEvent(DES_LEVIES_REQUEST, Some(empref))) {
      audit(new ALAEvent("readLevyDeclarations", empref, "", dateParams)) {
        des.httpGet.GET[EmployerPaymentsSummary](url)
      }
    }
  }
}

trait DesConnector extends DesUrl
                   with FractionsEndpoint
                   with EmployerDetailsEndpoint
                   with EmploymentCheckEndpoint
                   with LevyDeclarationsEndpoint
                   with Auditor
                   with GraphiteMetrics {
  def httpGet: HttpGet
}

object LiveDesConnector extends DesConnector with DesProductionUrl {
  def httpGet: HttpGet = WSHttp
  protected def auditConnector: Option[AuditConnector] = Some(MicroserviceAuditFilter.auditConnector)
}

object SandboxDesConnector extends DesConnector with DesSandboxUrl {
  def httpGet: HttpGet = WSHttp
  protected def auditConnector: Option[AuditConnector] = None
}
