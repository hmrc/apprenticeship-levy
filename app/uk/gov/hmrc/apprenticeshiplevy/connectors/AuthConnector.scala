/*
 * Copyright 2018 HM Revenue & Customs
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
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.config.MicroserviceAuditFilter
import uk.gov.hmrc.apprenticeshiplevy.audit.Auditor
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}
import play.api.{Logger,Mode}
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.apprenticeshiplevy.metrics._
import com.kenshoo.play.metrics.Metrics
import com.codahale.metrics.Counter
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet }

trait AuthConnector
  extends Auditor
  with GraphiteMetrics
  with Timer
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration {
  metrics: GraphiteMetrics =>

  def authBaseUrl: String

  def http: HttpGet

  def getEmprefs(implicit hc: HeaderCarrier): Future[Seq[String]] = {
    timer(RequestEvent(AUTH_SERVICE_REQUEST, None)) {
      audit(new ALAEvent("readEmprefs", "")) {
        http.GET[Authority](s"$authBaseUrl/auth/authority").map { a => a.accounts.epaye.map(_.empRef.value).toList}
      }
    }
  }
}

object SandboxAuthConnector
  extends AuthConnector
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration {
  override val authBaseUrl: String = AppContext.stubAuthUrl
  override val http: HttpGet = WSHttp
  protected def auditConnector: Option[AuditConnector] = None
}

object LiveAuthConnector
  extends AuthConnector
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration {
  override def authBaseUrl: String = AppContext.authUrl
  override def http: HttpGet = WSHttp
  protected def auditConnector: Option[AuditConnector] = Some(MicroserviceAuditFilter.auditConnector)
}
