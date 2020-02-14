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

package uk.gov.hmrc.apprenticeshiplevy.config

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.Play
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HttpGet, _}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig

trait Hooks extends uk.gov.hmrc.http.hooks.HttpHooks {
  override val hooks: Seq[HttpHook] = Seq.empty[HttpHook]
}

trait WSHttp extends HttpGet with WSGet
                 with HttpPut with WSPut
                 with HttpPost with WSPost
                 with HttpDelete with WSDelete
                 with HttpPatch with WSPatch
                 with Hooks{
}
object WSHttp extends WSHttp {
  override protected def actorSystem: ActorSystem = Play.current.actorSystem

  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
}


object MicroserviceAuditConnector
  extends AuditConnector
  with Configuration {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object MicroserviceAuthConnector
  extends AuthConnector
  with ServicesConfig
  with Configuration
  with WSHttp {
  override val authBaseUrl = AppContext.authUrl
  def http: HttpGet = WSHttp

  override protected def actorSystem: ActorSystem = Play.current.actorSystem

  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
}
