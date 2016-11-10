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

  def appName: String =
    current.configuration.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  def appUrl: String =
    current.configuration.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  def serviceLocatorUrl: String = baseUrl("service-locator")
  def registrationEnabled: Boolean =
    current.configuration.getString("microservice.services.service-locator.enabled")
      .flatMap(flag => Try(flag.toBoolean).toOption)
      .getOrElse {
        // $COVERAGE-OFF$
        Logger.warn("A configuration value has not been provided for service-locator.enabled, defaulting to true")
        // $COVERAGE-ON$
        true
      }

  def privateModeEnabled: Boolean = current.configuration.getString("microservice.private-mode")
    .flatMap(flag => Try(flag.toBoolean).toOption)
    .getOrElse {
      // $COVERAGE-OFF$
      Logger.warn("A configuration value has not been provided for microservice.private-mode, defaulting to true")
      // $COVERAGE-ON$
      true
    }

  def whitelistedApplicationIds: Seq[String] = current.configuration.getString("microservice.whitelisted-applications")
    .map { applicationIds => applicationIds.split(",").toSeq }
    .getOrElse(Seq.empty)

  // $COVERAGE-OFF$
  Logger.info(s"""\n${"*" * 80}\nWhite list:\n${whitelistedApplicationIds.mkString(", ")}\n${"*" * 80}\n""")
  // $COVERAGE-ON$

  def desUrl: String = baseUrl("des")
  def stubDesUrl: String = Try(baseUrl("stub-des") + getString(current.configuration)("microservice.services.stub-des.path")).getOrElse(baseUrl("stub-des"))
  def stubAuthUrl: String = Try(baseUrl("stub-auth") + getString(current.configuration)("microservice.services.stub-auth.path")).getOrElse(baseUrl("stub-auth"))

  def datePattern(): String = getString(current.configuration)("microservice.dateRegex")
  def employerReferencePattern(): String = getString(current.configuration)("microservice.emprefRegex")
  def ninoPattern(): String = getString(current.configuration)("microservice.ninoRegex")

  // scalastyle:off
  def defaultNumberOfDeclarationYears: Int =
    current.configuration.getString("microservice.defaultNumberOfDeclarationYears").map(_.toInt).getOrElse(6)
  // scalastyle:on

  private def getString(config: Configuration)(id: String): String = config.getString(id)
    .getOrElse(throw new RuntimeException(s"Unable to read whitelisted application (value '$id' not found)"))
}
