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

package uk.gov.hmrc.apprenticeshiplevy.data.api

import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec

class NinoSpec extends AppLevyUnitSpec {

  "isValid" should {
    "return true" when {
      "the NINO is the correct format" in {
        val validNino = "AA123456A"

        Nino.isValid(validNino) shouldBe true
      }
      "the NINO is an incorrect format" in {
        val invalidNino = "invalidNino"

        Nino.isValid(invalidNino) shouldBe false
      }
    }
  }
}
