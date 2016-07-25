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

package uk.gov.hmrc.apprenticeshiplevy.domain

import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.play.controllers.RestFormats

sealed trait EmploymentCheckStatus

object EmploymentCheckStatus {
  implicit val reads: Reads[EmploymentCheckStatus] = new Reads[EmploymentCheckStatus] {
    override def reads(json: JsValue) =
      implicitly[Reads[String]].reads(json).map {
        case "employed" => Employed
        case "not_employed" => NotEmployed
        case _ => NinoUnknown
      }
  }
}

case object Employed extends EmploymentCheckStatus

case object NotEmployed extends EmploymentCheckStatus

case object NinoUnknown extends EmploymentCheckStatus

case class EmploymentCheck(empref: String, nino: String, date: LocalDate, employed: Boolean)

object EmploymentCheck {
  implicit val dateFormats = RestFormats.localDateFormats
  implicit val formats = Json.format[EmploymentCheck]
}
