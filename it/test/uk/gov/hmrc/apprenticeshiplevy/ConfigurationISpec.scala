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

package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.apprenticeshiplevy.config.PathBinders
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData._
import util.WireMockHelper

class ConfigurationISpec
  extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with ScalaCheckPropertyChecks {

  override def fakeApplication(): Application = {
    val conf = wireMockConfiguration(server.port())
    GuiceApplicationBuilder()
      .configure(conf)
      .build()
  }

  "Application Configuration" should {
    "support NINO's with 'KC' prefix" in {
      PathBinders.isValidNino("KC745625A", "ERRORCODE").getOrElse(None) shouldBe "KC745625A"
    }
  }
}
