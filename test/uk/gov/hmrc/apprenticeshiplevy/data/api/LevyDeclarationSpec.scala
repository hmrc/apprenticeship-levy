/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDateTime

class LevyDeclarationSpec extends AppLevyUnitSpec {
  "LevyDeclaration" should {
    "have a rti submission field" in {
      val rtiId = 123L
      val levyDeclaration = LevyDeclaration(id = 456L, submissionTime = LocalDateTime.now(), submissionId = rtiId)
      levyDeclaration.submissionId shouldBe (rtiId)
    }

    "have a unique id field" in {
      val id = 456L
      val levyDeclaration = LevyDeclaration(id = id, submissionTime = LocalDateTime.now(), submissionId = 123534L)
      levyDeclaration.id shouldBe (id)
    }
  }
}
