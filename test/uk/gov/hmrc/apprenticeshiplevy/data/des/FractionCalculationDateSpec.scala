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

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import play.api.libs.json._
import play.api.libs.json.Json._
import org.joda.time.LocalDate

class FractionCalculationDateSpec extends UnitSpec {
  "FractionCalculationDate" should {
    "With Json" must {
      "read" in {
        // set up
        val jsonStr = """{"date": "2016-11-19"}"""
        val json = Json.parse(jsonStr)

        // test
         val fractionCalculationDateOption : Option[FractionCalculationDate] = json.validate[FractionCalculationDate].fold(invalid = { _ => None }, valid = { fcd => Some(fcd) })

        // check
        fractionCalculationDateOption shouldBe Some(FractionCalculationDate(new LocalDate(2016,11,19)))
      }

      "write" in {
        // set up
        val obj = FractionCalculationDate(new LocalDate(2015,3,6))

        // test
        val result = Json.toJson(obj)

        // check
        val date = result \ "date"
        date.as[String] shouldBe "2015-03-06"
      }
    }
  }
}
