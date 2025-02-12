/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.Json

import java.time.LocalDate

class DateRangeSpec extends AppLevyUnitSpec {

  "Date Range" should {

    "Parse ClosedDateRange json correctly" in {
      val json = Json.parse("""{"from":"2000-01-01","to":"2020-01-01"}""")
      json.validate[ClosedDateRange].isSuccess shouldBe true
      json.as[ClosedDateRange] shouldBe ClosedDateRange(LocalDate.of(2000,1,1), LocalDate.of(2020,1,1))
    }

    "Serialise OpenDateRange json correctly" in {
      val json = Json.parse("""{"to":"2020-01-01"}""")
      json.validate[OpenEarlyDateRange].isSuccess shouldBe true
      json.as[OpenEarlyDateRange] shouldBe OpenEarlyDateRange(LocalDate.of(2020,1,1))
    }
  }
}
