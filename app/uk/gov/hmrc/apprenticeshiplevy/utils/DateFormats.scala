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

package uk.gov.hmrc.apprenticeshiplevy.utils

import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import scala.util.Try

object DateFormats {

  val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val datePattern = DateTimeFormatter.ISO_LOCAL_DATE

  implicit def localDateTimeReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      Try(JsSuccess(LocalDateTime.parse(json.as[String], dateTimePattern), JsPath)).getOrElse(JsError())
  }

  implicit def localDateTimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
    def writes(localDateTime: LocalDateTime): JsValue = JsString(localDateTime.format(dateTimePattern))
  }

  implicit def localDateTimeFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)

  implicit def localDateReads: Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      Try(JsSuccess(LocalDate.parse(json.as[String], datePattern), JsPath)).getOrElse(JsError())
  }

  implicit def localDateWrites: Writes[LocalDate] = new Writes[LocalDate] {
    def writes(localDate: LocalDate): JsValue = JsString(localDate.format(datePattern))
  }

  implicit def localDateFormat: Format[LocalDate] = Format(localDateReads, localDateWrites)
}
