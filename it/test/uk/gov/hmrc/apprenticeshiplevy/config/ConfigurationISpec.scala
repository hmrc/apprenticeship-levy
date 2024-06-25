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

package test.uk.gov.hmrc.apprenticeshiplevy.config

import org.scalatest._
import org.scalatestplus.play._
import uk.gov.hmrc.apprenticeshiplevy._
import org.scalatest.matchers.should.Matchers
import test.uk.gov.hmrc.apprenticeshiplevy.WiremockFunSpec

@DoNotDiscover
class ConfigurationISpec extends WiremockFunSpec
with ConfiguredServer with EitherValues with Matchers {
  describe("Application Configuration") {
    it ("should support NINO's with 'KC' prefix") {
      PathBinders.isValidNino("KC745625A", "ERRORCODE").getOrElse(None) shouldBe "KC745625A"
    }
  }
}
