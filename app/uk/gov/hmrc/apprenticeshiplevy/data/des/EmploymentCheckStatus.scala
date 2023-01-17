/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.json.Reads._
import play.api.libs.json._

sealed trait EmploymentCheckStatus

object EmploymentCheckStatus {
  implicit val reads: Reads[EmploymentCheckStatus] = (__ \ "employed").read[Boolean]
                                                      .orElse((__ \ "employed").read[String].map(_.toBoolean))
                                                      .map(isEmployed => EmploymentCheckStatus(isEmployed))

  def apply(isEmployed: Boolean): EmploymentCheckStatus = if (isEmployed) {
    Employed
  } else {
    NotEmployed
  }

  def apply(isEmployed: String): EmploymentCheckStatus = isEmployed match {
    case "true" => Employed
    case "false" => NotEmployed
  }
}

case object Employed extends EmploymentCheckStatus

case object NotEmployed extends EmploymentCheckStatus

case object Unknown extends EmploymentCheckStatus
