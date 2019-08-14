/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.api.ServiceLocatorRegistration
import uk.gov.hmrc.apprenticeshiplevy.data.api.ServiceLocatorRegistration._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class ServiceLocatorConnector @Inject()(http: WSHttp) {
  lazy val appName = AppContext.maybeString("appName").getOrElse("apprenticeship-levy")
  lazy val appUrl = AppContext.appUrl
  lazy val serviceUrl = AppContext.serviceLocatorUrl
  val metadata = Some(Map("third-party-api" -> "true"))

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


