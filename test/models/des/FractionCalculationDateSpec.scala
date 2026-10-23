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

package models.des

import play.api.libs.json.*
import utils.SpecBase

import java.time.LocalDate

class FractionCalculationDateSpec extends SpecBase {
  "FractionCalculationDate" must {
    "With Json" must {
      "read" in {
        val jsonStr = """{"date": "2016-11-19"}"""
        val json = Json.parse(jsonStr)

         val fractionCalculationDateOption : Option[FractionCalculationDate] = json.validate[FractionCalculationDate].fold(invalid = { _ => None }, valid = { fcd => Some(fcd) })

        fractionCalculationDateOption mustBe Some(FractionCalculationDate(LocalDate.of(2016,11,19)))
      }

      "write" in {
        val obj = FractionCalculationDate(LocalDate.of(2015,3,6))

        val result = Json.toJson(obj)

        val date = result \ "date"
        date.as[String] mustBe "2015-03-06"
      }
    }
  }
}
