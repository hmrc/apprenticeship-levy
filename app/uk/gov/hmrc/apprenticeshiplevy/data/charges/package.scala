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

package uk.gov.hmrc.apprenticeshiplevy.data

import org.joda.time.LocalDateTime
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

package object charges {

  implicit val ldtFormats = new Format[LocalDateTime] {
    val fmt = org.joda.time.format.DateTimeFormat.forPattern("dd-MM-YYYY'T'HH:mm:ss")

    override def writes(ldt: LocalDateTime): JsValue = JsString(fmt.print(ldt))

    override def reads(json: JsValue): JsResult[LocalDateTime] = implicitly[Reads[String]].reads(json).flatMap { s =>
      Try(fmt.parseLocalDateTime(s)) match {
        case Failure(t) => JsError(t.getMessage)
        case Success(ldt) => JsSuccess(ldt)
      }
    }
  }

  implicit val periodFormats = Json.format[Period]
  implicit val chargeFormats = Json.format[Charge]
  implicit val chargesFormats = Json.format[Charges]
}
