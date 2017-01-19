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

package uk.gov.hmrc.apprenticeshiplevy.config

import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalatestplus.play.OneAppPerSuite

class NinoValidationTest extends WordSpec with EitherValues with Matchers with OneAppPerSuite {

  "The validation of a nino" should {
    "pass with 'KC' prefix" in {
      PathBinders.isValid(PathBinders.NinoPattern, "KC745625A").right.value shouldBe "KC745625A"
    }
  }
}
