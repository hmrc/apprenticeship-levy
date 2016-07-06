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

import uk.gov.hmrc.apprenticeshiplevy.config.WSHttp
import uk.gov.hmrc.apprenticeshiplevy.data.epaye.DesignatoryDetails
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper

import scala.concurrent.Future

trait EpayeConnector {
  def epayeBaseUrl: String

  def http: HttpGet

  def designatoryDetails(empref: String)(implicit hc: HeaderCarrier): Future[DesignatoryDetails] = {
    http.GET[DesignatoryDetails](s"$epayeBaseUrl/epaye/${helper.urlEncode(empref)}/designatory-details")
  }

}
object SandboxEpayeConnector extends EpayeConnector with ServicesConfig {
  override val epayeBaseUrl: String = baseUrl("stub-epaye")
  override val http: HttpGet = WSHttp
}

object LiveEpayeConnector extends EpayeConnector with ServicesConfig {
  override def epayeBaseUrl: String = baseUrl("epaye")
  override def http: HttpGet = WSHttp
}
