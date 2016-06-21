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

package uk.gov.hmrc.apprenticeshiplevy.config

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.ETMPConnector
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.Future

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName {
  override val hooks: Seq[HttpHook] = NoneRequired
}

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig {
  override val authBaseUrl = baseUrl("auth")

  def http: HttpGet = WSHttp

  def currentAuthority(implicit hc: HeaderCarrier): Future[Option[Authority]] = {
    http.GET[Authority](s"$authBaseUrl/auth/authority").map(Some.apply) // Option return is legacy of previous http library now baked into this class's api
  }
}

