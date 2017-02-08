/*
 * Copyright 2017 HM Revenue & Customs
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

import scala.concurrent.{Future, ExecutionContext}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.play.audit.EventKeys._
import scala.util.{Success, Failure, Try}
import play.api.Logger
import uk.gov.hmrc.play.http._
import java.io.IOException

trait Auditor  {
  def audit[T](event: ALAEvent)(block: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    block andThen {
      case Success(v) => auditConnector.map(_.sendEvent(event.toDataEvent(200)))
      case Failure(t) => {
        val httpStatus = exceptionToMessage(t)
        auditConnector.map(_.sendEvent(event.toDataEvent(httpStatus)))
        Logger.error(s"Failed to '${event.name}' Server ${httpStatus}: ${t.getMessage()}",t)
      }
    }
  }

  protected def auditConnector: Option[AuditConnector]
  protected val exceptionToMessage: PartialFunction[Throwable, Int] = {
        case e: BadRequestException => 400
        case e: IOException => 444
        case e: GatewayTimeoutException => 408
        case e: NotFoundException => 404
        case e: Upstream5xxResponse => e.upstreamResponseCode
        case e: Upstream4xxResponse => e.upstreamResponseCode
        case e => 500
    }
}
