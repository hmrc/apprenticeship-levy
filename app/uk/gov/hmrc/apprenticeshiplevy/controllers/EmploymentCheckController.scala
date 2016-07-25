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
import play.api.mvc.Action
import uk.gov.hmrc.apprenticeshiplevy.connectors.RTIConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.ErrorNinoNotVisible
import uk.gov.hmrc.apprenticeshiplevy.domain.{Employed, EmploymentCheck, NinoUnknown, NotEmployed}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

trait EmploymentCheckController extends ApiController {

  def rtiConnector: RTIConnector
  def check(empref: String, nino: String, atDate: Option[LocalDate]) = Action.async { implicit request =>
    val checkDate = atDate.getOrElse(LocalDate.now)

    rtiConnector.check(empref, nino, checkDate).map {
      case Employed => Ok(Json.toJson(EmploymentCheck(empref, nino, checkDate, employed = true)))
      case NotEmployed => Ok(Json.toJson(EmploymentCheck(empref, nino, checkDate, employed = false)))
      case NinoUnknown => ErrorNinoNotVisible.toResult
    }
  }

}