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

package uk.gov.hmrc.apprenticeshiplevy.config

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api._
import play.api.mvc._
import play.filters.headers._
import uk.gov.hmrc.apprenticeshiplevy.connectors.ServiceLocatorConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import scala.util.{Try, Success, Failure}
import play.Logger
import uk.gov.hmrc.apprenticeshiplevy.config.filters._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.filters.{ AuditFilter, LoggingFilter, MicroserviceFilterSupport }

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = AppContext.maybeConfiguration.map(_.underlying.as[Config]("controllers")).getOrElse(throw new RuntimeException())
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceAuditFilter
  extends AuditFilter
  with MicroserviceFilterSupport
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration {
  override val auditConnector = MicroserviceAuditConnector
  override def appName = AppContext.maybeString("appName").getOrElse("apprenticeship-levy")
  override def controllerNeedsAuditing(controllerName: String): Boolean =
    ControllerConfiguration.paramsForController(controllerName).needsAuditing

  override protected def needsAuditing(request: RequestHeader): Boolean = if (runMode == "Test") false else super.needsAuditing(request)

  protected lazy val runMode = Try(env) match {
    case Success(m) => m
    case Failure(_) => {
      // $COVERAGE-OFF$
      Logger.warn("Run mode not set. Is Play Application running?")
      // $COVERAGE-ON$
      "Dev"
    }
  }
}

object MicroserviceAPIHeaderCaptureFilter
  extends APIHeaderCaptureFilter
  with MicroserviceFilterSupport
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration

object MicroserviceLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean =
    ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object MicroserviceAuthFilter extends AuthorisationFilter with MicroserviceFilterSupport {
  override lazy val authParamsConfig = AuthParamsControllerConfiguration
  override lazy val authConnector = MicroserviceAuthConnector

  override def controllerNeedsAuth(controllerName: String): Boolean =
    ControllerConfiguration.paramsForController(controllerName).needsAuth
}

object MicroserviceGlobal
  extends DefaultMicroserviceGlobal
  with uk.gov.hmrc.apprenticeshiplevy.config.Configuration {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[play.api.Configuration] =
    app.configuration.getConfig(s"microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  override protected lazy val defaultMicroserviceFilters: Seq[EssentialFilter] = Try {
    super.defaultMicroserviceFilters ++ Seq(new SecurityHeadersFilter(SecurityHeadersConfig.fromConfiguration(AppContext.maybeConfiguration.get)),
                                            MicroserviceAPIHeaderCaptureFilter)
    } match {
      case Success(v) => v
      case Failure(e) => {
        // $COVERAGE-OFF$
        Logger.error(s"Failed to initialise filters. ${e.getMessage}")
        // $COVERAGE-ON$
        Seq.empty
      }
    }
}
