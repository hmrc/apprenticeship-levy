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
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.PayrollMonth
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper

import scala.concurrent.Future

case class ETMPLevyDeclaration(payrollMonth: PayrollMonth, amount: BigDecimal, submissionType: String, submissionDate: String)

object ETMPLevyDeclaration {
  implicit val formats = Json.format[ETMPLevyDeclaration]
}

case class ETMPLevyDeclarations(empref: String, schemeCessationDate: Option[LocalDate], declarations: Seq[ETMPLevyDeclaration])

object ETMPLevyDeclarations {
  implicit val formats = Json.format[ETMPLevyDeclarations]
}


trait ETMPConnector {

  def etmpBaseUrl: String

  def httpGet: HttpGet

  def declarations(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[ETMPLevyDeclarations] = {
    val url = (s"$etmpBaseUrl/empref/${helper.urlEncode(empref)}/declarations", months) match {
      case (u, Some(n)) => s"$u/?months=$n"
      case (u, None) => u
    }

    Logger.debug(s"Calling ETMP at $url")

    httpGet.GET[ETMPLevyDeclarations](url)
  }
}

object LiveETMPConnector extends ETMPConnector {
  override def etmpBaseUrl = AppContext.etmpUrl

  override def httpGet = WSHttp
}

object StubETMPConnector extends ETMPConnector {
  override def etmpBaseUrl = AppContext.stubEtmpUrl

  override def httpGet = WSHttp
}
