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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.connectors.RTIConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.ErrorNinoNotVisible
import uk.gov.hmrc.apprenticeshiplevy.domain.{Employed, EmploymentCheck, NinoUnknown, NotEmployed}
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future
import play.api.mvc.Result

trait EmploymentCheckController extends ApiController {

  def rtiConnector: RTIConnector

  def check(empref: String, nino: String, fromDate: LocalDate, toDate: LocalDate) = withValidAcceptHeader.async { implicit request =>
    rtiConnector.check(empref, nino, ClosedDateRange(fromDate, toDate)).map {
      case Employed => Ok(Json.toJson(EmploymentCheck(empref, nino, fromDate, toDate, employed = true)))
      case NotEmployed => Ok(Json.toJson(EmploymentCheck(empref, nino, fromDate, toDate, employed = false)))
      case NinoUnknown => ErrorNinoNotVisible.toResult
    }
  }
}
