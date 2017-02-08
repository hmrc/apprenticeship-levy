/*
 * Copyright 2017 HM Revenue & Customs
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

class PackageSpec extends UnitSpec {
  "Data Des Package" should {
    "With Joda Local Date" must {
      "provide json read" in {
        // test
        val result = Json.toJson("2014-02-05")

        // check
        result shouldBe a[JsString]
        result.as[String] shouldBe "2014-02-05"
        result.as[LocalDate] shouldBe new LocalDate("2014-02-05")
      }
      "provide json write" in {
        // test
        val result = Json.toJson(new LocalDate("2014-03-05"))

        // check
        result shouldBe a[JsString]
        result.as[String] shouldBe "2014-03-05"
        result.as[LocalDate] shouldBe new LocalDate("2014-03-05")
      }
    }
  }
}
