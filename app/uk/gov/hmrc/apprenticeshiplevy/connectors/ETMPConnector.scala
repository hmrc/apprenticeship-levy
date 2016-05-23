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
import uk.gov.hmrc.apprenticeshiplevy.data.LevyDeclarations
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper

import scala.concurrent.Future

object ETMPConnector extends ETMPConnector {
  override def etmpBaseUrl: String = AppContext.etmpUrl

  override def httpGet: HttpGet = WSHttp
}

trait ETMPConnector {

  def etmpBaseUrl: String

  def httpGet: HttpGet

  def declarations(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[LevyDeclarations] = {
    val url = (s"$etmpBaseUrl/apprenticeship-levy-stub/empref/${helper.urlEncode(empref)}/declarations", months) match {
      case (u, Some(n)) => s"$u/?months=$n"
      case (u, None) => u
    }

    Logger.debug(s"ETMP url is $url")

    httpGet.GET[LevyDeclarations](url)
  }


}
