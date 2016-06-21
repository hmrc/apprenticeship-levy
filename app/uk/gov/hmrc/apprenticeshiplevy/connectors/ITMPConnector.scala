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
import uk.gov.hmrc.apprenticeshiplevy.data.Fractions
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper

import scala.concurrent.Future

trait ITMPConnector {

  def itmpBaseUrl: String

  def httpGet: HttpGet

  def fractions(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[Fractions] = {
    val url = (s"$itmpBaseUrl/empref/${helper.urlEncode(empref)}/fractions", months) match {
      case (u, Some(n)) => s"$u/?months=$n"
      case (u, None) => u
    }

    Logger.debug(s"Calling ITMP at $url")
    httpGet.GET[Fractions](url)
  }
}

object LiveITMPConnector extends ITMPConnector {
  override def itmpBaseUrl = AppContext.etmpUrl

  override def httpGet = WSHttp
}

object StubITMPConnector extends ITMPConnector {
  override def itmpBaseUrl = AppContext.stubItmpUrl

  override def httpGet = WSHttp
}
