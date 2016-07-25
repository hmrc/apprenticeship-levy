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
import uk.gov.hmrc.apprenticeshiplevy.domain.EmploymentCheckStatus
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.time.DateConverter
import views.html.helper

import scala.concurrent.Future

trait RTIConnector {

  def rtiBaseUrl: String

  def httpGet: HttpGet

  def eps(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[Seq[EmployerPaymentSummary]] = {
    val url = (s"$rtiBaseUrl/epaye/${helper.urlEncode(empref)}/eps", months) match {
      case (u, Some(n)) => s"$u/?months=$n"
      case (u, None) => u
    }

    Logger.debug(s"Calling RTI at $url")

    httpGet.GET[Seq[EmployerPaymentSummary]](url)
  }

  def check(empref: String, nino: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[EmploymentCheckStatus] = {
    val params =
      Seq("fromDate" -> fromDate, "toDate" -> toDate)
        .map(p => s"${p._1}=${DateConverter.formatToString(p._2)}")
        .mkString("&")

    val url = s"$rtiBaseUrl/empref/${helper.urlEncode(empref)}/employee/${helper.urlEncode(nino)}?$params"

    Logger.debug(s"Calling RTI at $url")

    httpGet.GET[EmploymentCheckStatus](url)
  }
}

object LiveRTIConnector extends RTIConnector {
  override def rtiBaseUrl = AppContext.rtiUrl

  override def httpGet = WSHttp
}

object SandboxRTIConnector extends RTIConnector {
  override def rtiBaseUrl = AppContext.stubRtiUrl

  override def httpGet = WSHttp
}
