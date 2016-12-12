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
import uk.gov.hmrc.apprenticeshiplevy.data.api.ServiceLocatorRegistration
import uk.gov.hmrc.apprenticeshiplevy.data.api.ServiceLocatorRegistration._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait ServiceLocatorConnector {
  val appName: String
  val appUrl: String
  val serviceUrl: String
  val metadata: Option[Map[String, String]]
  val http: HttpPost

  def register(implicit hc: HeaderCarrier): Future[Try[Unit]] =
    http
      .POST(
        url = s"$serviceUrl/registration",
        body = ServiceLocatorRegistration(appName, appUrl, metadata),
        headers = Seq("Content-Type" -> "application/json"))
      .map { _ =>
        // $COVERAGE-OFF$
        Logger.info("Service successfully registered on the service locator")
        // $COVERAGE-ON$
        Success(())
      }
      .recover { case NonFatal(e) =>
          // $COVERAGE-OFF$
          Logger.error(s"Service could not register on the service locator", e)
          // $COVERAGE-ON$
          Failure(e)
        case e: RuntimeException =>
          // $COVERAGE-OFF$
          Logger.error(s"Service could not register on the service locator", e)
          // $COVERAGE-ON$
          Failure(e)
      }
}

object ServiceLocatorConnector extends ServiceLocatorConnector {
  override lazy val appName = AppContext.appName
  override lazy val appUrl = AppContext.appUrl
  override lazy val serviceUrl = AppContext.serviceLocatorUrl
  override val http = WSHttp
  override val metadata = Some(Map("third-party-api" -> "true"))
}
