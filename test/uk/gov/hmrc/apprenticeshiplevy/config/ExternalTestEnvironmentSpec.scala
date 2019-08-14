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

package uk.gov.hmrc.apprenticeshiplevy.config

import org.scalatestplus.play._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class ExternalTestEnvironmentSpec extends UnitSpec with OneAppPerSuite {
  "When in External Test Mode API" must {
    val mockExternalTestRoutes = app.injector.instanceOf[externaltest.Routes]
    val mocknonexternalTestRoutes = app.injector.instanceOf[nonexternaltest.Routes]

    object TestRouter extends ConditionalRouter(mockExternalTestRoutes, mocknonexternalTestRoutes) with IsInExternalTest {
      override def isInExternalTest: Boolean = true
    }

    "not have sandbox API endpoints" in {
      // set up
      val request = FakeRequest(GET, "/sandbox/epaye/123%2FAB12345/fractions")

      // test
      val result = TestRouter.handlerFor(request)

      // check
      TestRouter.routes === mockExternalTestRoutes.routes
      result shouldBe None
    }

    "have production API endpoints" in {
      // set up
      val request = FakeRequest(GET, "/epaye/123%2FAB12345/fractions")

      // test
      val result = TestRouter.handlerFor(request)

      // check
      TestRouter.routes === mocknonexternalTestRoutes.routes
      result.get === uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxFractionsController.fractions _
    }
  }
}
