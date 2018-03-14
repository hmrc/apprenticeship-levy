/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatest.DoNotDiscover
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import org.scalatest._
import org.scalatest.Matchers._

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play._
import uk.gov.hmrc.apprenticeshiplevy.config._

class ExternalTestEnvironmentSpec extends UnitSpec {
  "When in External Test Mode API" must {
    object TestRouter extends ConditionalRouter with IsInExternalTest {
      def isInExternalTest: Boolean = true
    }

    "not have sandbox API endpoints" in {
      // set up
      val request = FakeRequest(GET, "/sandbox/epaye/123%2FAB12345/fractions")

      // test
      val result = TestRouter.handlerFor(request)

      // check
      TestRouter.routes === externaltest.Routes.routes
      result shouldBe None
    }

    "have production API endpoints" in {
      // set up
      val request = FakeRequest(GET, "/epaye/123%2FAB12345/fractions")

      // test
      val result = TestRouter.handlerFor(request)

      // check
      TestRouter.routes === externaltest.Routes.routes
      result.get === uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.SandboxFractionsController.fractions _
    }
  }
}
