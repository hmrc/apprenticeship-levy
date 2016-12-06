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

package uk.gov.hmrc.apprenticeshiplevy.audit

import scala.concurrent.{Future, ExecutionContext}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.apprenticeshiplevy.data.audit.ALAEvent
import uk.gov.hmrc.play.audit.EventKeys._
import scala.util.{Success, Failure, Try}
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

trait Auditor  {
  def audit[T](event: ALAEvent)(block: => Future[T])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[T] = {
    block andThen {
      case Success(v) => auditConnector.map(_.sendEvent(event.toDataEvent(true)))
      case Failure(t) => {
        auditConnector.map(_.sendEvent(event.toDataEvent(false)))
        Logger.error(s"Failed to '${event.name}' ${t.getMessage()}",t)
      }
    }
  }

  protected def auditConnector: Option[AuditConnector]
}
