/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, REQUEST_TIMEOUT}
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.http.{BadRequestException, GatewayTimeoutException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.io.IOException
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait Auditor extends Logging {

  protected val exceptionToMessage: PartialFunction[Throwable, Int] = {
    case _: BadRequestException => BAD_REQUEST
    case _: IOException => NGINX_ERROR_STATUS
    case _: GatewayTimeoutException => REQUEST_TIMEOUT
    case _: NotFoundException => NOT_FOUND
    case e: UpstreamErrorResponse => e.statusCode
    case _ => INTERNAL_SERVER_ERROR
  }
  private val NGINX_ERROR_STATUS: Int = 444

  def audit[T](event: ALAEvent)(block: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    block andThen {
      case Success(_) => auditConnector.map(_.sendEvent(event.toDataEvent(200)))
      case Failure(t) => {
        val httpStatus = exceptionToMessage(t)
        auditConnector.map(_.sendEvent(event.toDataEvent(httpStatus, t)))
        logger.warn(s"Failed to '${event.name}' Server ${httpStatus}: ${t.getMessage()}")
      }
    }
  }

  protected def auditConnector: Option[AuditConnector]
}


