/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import org.joda.time.{LocalDate, LocalDateTime}

class LevyDeclarationSpec extends UnitSpec {
  "LevyDeclaration" should {
    "have a rti submission field" in {
      val rtiId = 123L
      val levyDeclaration = LevyDeclaration(id = 456L, submissionTime = new LocalDateTime(), submissionId = rtiId)
      levyDeclaration.submissionId shouldBe (rtiId)
    }

    "have a unique id field" in {
      val id = 456L
      val levyDeclaration = LevyDeclaration(id = id, submissionTime = new LocalDateTime(), submissionId = 123534L)
      levyDeclaration.id shouldBe (id)
    }
  }
}
