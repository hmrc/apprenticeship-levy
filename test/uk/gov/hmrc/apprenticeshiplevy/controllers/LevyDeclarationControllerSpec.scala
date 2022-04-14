/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import play.api.inject.bind
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.connectors.LiveDesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{FakePrivilegedAuthAction, PrivilegedAuthActionImpl}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.utils.{AppLevyUnitSpec, MockAppContext}

class LevyDeclarationControllerSpec extends AppLevyUnitSpec with ScalaFutures with GuiceOneAppPerSuite
  with Injecting with BeforeAndAfterEach{

  val mockDesConnector = mock[LiveDesConnector]
  val stubComponents = stubControllerComponents()
  val mockAppContext = MockAppContext

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDesConnector)
    mockAppContext.reset()
  }

  override def fakeApplication: Application = GuiceApplicationBuilder()
    .overrides(
      bind[LiveDesConnector].toInstance(mockDesConnector),
      bind[PrivilegedAuthActionImpl].to[FakePrivilegedAuthAction],
      bind[ControllerComponents].toInstance(stubComponents),
      bind[AppContext].toInstance(mockAppContext.mocked)
    )
    .build()

  val liveFractionsController = inject[LiveLevyDeclarationController]

  "getting the levy declarations" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = liveFractionsController.declarations(EmploymentReference("empref"), None, None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }
  }
}

