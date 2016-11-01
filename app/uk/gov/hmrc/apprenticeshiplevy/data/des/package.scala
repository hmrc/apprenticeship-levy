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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate

package object des {
  implicit val jodaDateFormat = new Format[LocalDate] {
    val localDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

    override def reads(json: JsValue): JsResult[LocalDate] = implicitly[Reads[JsString]].reads(json).map { js =>
      localDateFormat.parseDateTime(js.value).toLocalDate
    }

    override def writes(date: LocalDate): JsValue = JsString(localDateFormat.print(date))
  }
}
