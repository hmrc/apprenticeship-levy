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

import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.utils.{ClosedDateRange, DateRange}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, NotFoundException}
import views.html.helper

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait RTIConnector {

  def rtiBaseUrl: String

  def httpGet: HttpGet

  def eps(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier): Future[EmployerPaymentsSummary] = {

    val url = (s"$rtiBaseUrl/rti/employers/${helper.urlEncode(empref)}/employer-payment-summary", dateRange.toParams) match {
      case (u, None) => u
      case (u, Some(ps)) => s"$u?$ps"
    }

    // $COVERAGE-OFF$
    Logger.debug(s"Calling RTI at $url")
    // $COVERAGE-ON$

    httpGet.GET[EmployerPaymentsSummary](url)
  }

  def check(empref: String, nino: String, dateRange: ClosedDateRange)(implicit hc: HeaderCarrier): Future[EmploymentCheckStatus] = {
    val url = s"$rtiBaseUrl/apprenticeship-levy/employers/${helper.urlEncode(empref)}/employed/${helper.urlEncode(nino)}?${dateRange.paramString}"

    // $COVERAGE-OFF$
    Logger.debug(s"Calling RTI at $url")
    // $COVERAGE-ON$

    httpGet.GET[EmploymentCheckStatus](url) recover { case notFound: NotFoundException => NinoUnknown }
  }
}

object LiveRTIConnector extends RTIConnector {
  override def rtiBaseUrl: String = AppContext.rtiUrl

  override def httpGet: HttpGet = WSHttp
}

object SandboxRTIConnector extends RTIConnector {
  override def rtiBaseUrl: String = AppContext.stubRtiUrl

  override def httpGet: HttpGet = WSHttp
}
