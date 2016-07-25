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

import play.api.Play._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.util.Try

object AppContext extends ServicesConfig {

  lazy val appName =
    current.configuration.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  lazy val appUrl =
    current.configuration.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  lazy val serviceLocatorUrl = baseUrl("service-locator")
  lazy val registrationEnabled =
    current.configuration.getString("microservice.services.service-locator.enabled")
      .flatMap(flag => Try(flag.toBoolean).toOption)
      .getOrElse {
        Logger.warn("A configuration value has not been provided for service-locator.enabled, defaulting to true")
        true
      }

  lazy val privateModeEnabled = current.configuration.getString("microservice.private-mode")
    .flatMap(flag => Try(flag.toBoolean).toOption)
    .getOrElse {
      Logger.warn("A configuration value has not been provided for microservice.private-mode, defaulting to true")
      true
    }

  lazy val whitelistedApplicationIds = current.configuration.getString("microservice.whitelisted-applications")
    .map { applicationIds => applicationIds.split(",").toSeq }
    .getOrElse(Seq.empty)

  lazy val edhUrl = baseUrl("edh")
  lazy val stubEdhUrl = baseUrl("stub-edh")

  lazy val rtiUrl = baseUrl("rti")
  lazy val stubRtiUrl = baseUrl("stub-rti")

  private def getString(config: Configuration)(id: String): String = config.getString(id)
    .getOrElse(throw new RuntimeException(s"Unable to read whitelisted application (value '$id' not found)"))
}
