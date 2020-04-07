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

import com.typesafe.config.ConfigFactory
import play.api.{Application, Logger, Mode, Play}
import uk.gov.hmrc.play.config.{RunMode, ServicesConfig}

import scala.util.{Failure, Success, Try}

trait Configuration extends RunMode with ServicesConfig {
  private val nilConfig = play.api.Configuration(ConfigFactory.load())
  def appNameConfiguration: play.api.Configuration = AppContext.maybeConfiguration.flatMap(_.getConfig("appName")).getOrElse(nilConfig)
  protected def mode: Mode.Mode = AppContext.maybeApp.map(_.mode).getOrElse(Mode.Prod)
  protected def runModeConfiguration: play.api.Configuration = AppContext.maybeApp.map(_.configuration).getOrElse(nilConfig)
  def maybeBoolean(id: String): Option[Boolean] = AppContext.maybeConfiguration.flatMap(_.getBoolean(id))

  def maybeString(id: String): Option[String] = AppContext.maybeConfiguration.flatMap(_.getString(id))

  def maybeBaseURL(name: String): Option[String] = Try(baseUrl(name)) match {
        case Success(v) => Some(v)
        case Failure(e) => {
          // $COVERAGE-OFF$
          Logger.error(s"Unable to get baseUrl for ${name}. Error: ${e.getMessage()}")
          // $COVERAGE-ON$
          None
        }
      }
}

object AppContext extends Configuration {
  // $COVERAGE-OFF$
  Logger.info(s"""\n${"_" * 80}\n""")
  // $COVERAGE-ON$

  def maybeApp: Option[Application] = Try(Play.maybeApplication).getOrElse(None)

  def maybeConfiguration: Option[play.api.Configuration] = maybeApp.map(_.configuration)

  def appUrl: String = maybeString("appUrl").getOrElse{
    // $COVERAGE-OFF$
    Logger.error("appUrl is not configured")
    // $COVERAGE-ON$
    ""
  }

  def privateModeEnabled: Boolean = maybeString("microservice.private-mode")
    .flatMap(flag => Try(flag.toBoolean).toOption)
    .getOrElse {
      // $COVERAGE-OFF$
      Logger.warn("A configuration value has not been provided for microservice.private-mode, defaulting to true")
      // $COVERAGE-ON$
      true
    }

  def externalTestModeEnabled: Boolean = maybeString("microservice.external-test-mode")
    .flatMap(flag => Try(flag.toBoolean).toOption)
    .getOrElse {
      // $COVERAGE-OFF$
      Logger.debug("A configuration value has not been provided for microservice.external-test-mode, defaulting to false")
      // $COVERAGE-ON$
      false
    }

  def whitelistedApplicationIds: Seq[String] = maybeString("microservice.whitelisted-applications")
    .map { applicationIds => applicationIds.split(",").toSeq }.getOrElse(Seq.empty)

  // $COVERAGE-OFF$
  Logger.info(s"""\n${"*" * 80}\nWhite list:\n${whitelistedApplicationIds.mkString(", ")}\n${"*" * 80}\n""")
  // $COVERAGE-ON$

  def desEnvironment: String = maybeString("microservice.services.des.env").getOrElse("")

  def desToken: String = maybeString("microservice.services.des.token").getOrElse("")

  def metricsEnabled: Boolean = maybeString("microservice.metrics.graphite.enabled").flatMap(flag => Try(flag.toBoolean).toOption).getOrElse(false)

  def getURL(name: String) = Try {
      val url = maybeBaseURL(name).getOrElse("")
      val host = maybeString(s"microservice.services.${name}.host").getOrElse("")
      val path = maybeString(s"microservice.services.${name}.path").getOrElse("")
      val port = maybeString(s"microservice.services.${name}.port").getOrElse("")
      val protocol = maybeString(s"microservice.services.${name}.protocol").getOrElse("")
      if (port.isEmpty) {
        s"${protocol}://${host}"
      } else {
        val baseurl = if (maybeApp.map(_.mode).getOrElse(Mode.Test) == Mode.Prod && !url.contains("localhost")) appUrl else url
        if (path == "") url else s"${baseurl}${path}"
      }
    }.getOrElse(maybeBaseURL(name).getOrElse(""))

  def authUrl: String = getURL("auth")

  def desUrl: String = getURL("des")

  def stubURL(name: String) = Try {
      val stubUrl = maybeBaseURL(s"stub-${name}").getOrElse("")
      val path = maybeString(s"microservice.services.stub-${name}.path").getOrElse("")
      val baseurl = if (maybeApp.map(_.mode).getOrElse(Mode.Test) == Mode.Prod && !stubUrl.contains("localhost")) appUrl else stubUrl
      s"${baseurl}${path}"
    }.getOrElse(maybeBaseURL(s"stub-${name}").getOrElse(""))

  def stubDesUrl: String = stubURL("des")

  def stubAuthUrl: String = stubURL("auth")

  // $COVERAGE-OFF$
  Logger.info(s"""\nStub: DES URL: ${stubDesUrl}    Stub Auth URL: ${stubAuthUrl}""")
  Logger.info(s"""\nDES URL: ${desUrl}    AUTH URL: ${authUrl}""")
  // $COVERAGE-ON$

  def datePattern(): String = maybeString("microservice.dateRegex").getOrElse("")

  def employerReferencePattern(): String = maybeString("microservice.emprefRegex").getOrElse("")

  def ninoPattern(): String = maybeString("microservice.ninoRegex").getOrElse("")

  def epsOrigPathEnabled(): Boolean = maybeBoolean("microservice.epsOrigPathEnabled").getOrElse(true)

  // $COVERAGE-OFF$
  Logger.info(s"""\nWhite list:\n${whitelistedApplicationIds.mkString(", ")}\n""")
  // $COVERAGE-ON$

  // scalastyle:off
  def defaultNumberOfDeclarationYears: Int = maybeString("microservice.defaultNumberOfDeclarationYears").map(_.toInt).getOrElse(6)
  // scalastyle:on

  // $COVERAGE-OFF$
  Logger.info(s"""\n${"_" * 80}\n""")
  // $COVERAGE-ON$
}
