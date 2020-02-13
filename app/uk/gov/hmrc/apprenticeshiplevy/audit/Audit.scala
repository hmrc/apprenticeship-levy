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

package uk.gov.hmrc.apprenticeshiplevy.audit

import java.io.IOException

import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait Auditor  {
  def audit[T](event: ALAEvent)(block: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    block andThen {
      case Success(_) => auditConnector.map(_.sendEvent(event.toDataEvent(200)))
      case Failure(t) => {
        val httpStatus = exceptionToMessage(t)
        auditConnector.map(_.sendEvent(event.toDataEvent(httpStatus, t)))
        Logger.warn(s"Failed to '${event.name}' Server ${httpStatus}: ${t.getMessage()}")
      }
    }
  }

  protected def auditConnector: Option[AuditConnector]
  protected val exceptionToMessage: PartialFunction[Throwable, Int] = {
        case _: BadRequestException => 400
        case _: IOException => 444
        case _: GatewayTimeoutException => 408
        case _: NotFoundException => 404
        case e: Upstream5xxResponse => e.upstreamResponseCode
        case e: Upstream4xxResponse => e.upstreamResponseCode
        case _ => 500
    }
}
