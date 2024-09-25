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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import java.io.File

import org.scalatest.{BeforeAndAfterEach, Inside}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Environment, Mode}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import org.mockito.Mockito.when
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.apprenticeshiplevy.utils.{AppLevyUnitSpec, MockAppContext}

import scala.util.{Failure, Success}

class DocumentationControllerSpec extends AppLevyUnitSpec with Inside with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  val validDefinition = new File(getClass.getResource("/validDefinition.json").toURI)
  val invalidDefinition = new File(getClass.getResource("/invalidDefinition.json").toURI)
  val stubComponents: ControllerComponents = stubControllerComponents()
  val mockAppContext: MockAppContext.type = MockAppContext

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockAppContext.reset()
  }

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .overrides(
      bind[ControllerComponents].toInstance(stubComponents),
      bind[AppContext].toInstance(mockAppContext.mocked)
    )
    .build()

  val documentationController: DocumentationController = inject[DocumentationController]

  "DocumentationController" should {
    "add allowlist information correctly" in {
      when(mockAppContext.mocked.allowlistedApplicationIds).thenReturn(Seq("f0e2611e-2f45-4326-8cd2-6eefebec77b7", "cafebabe-2f45-4326-8cd2-6eefebec77b7"))
      val enrichedDefinition = documentationController.enrichDefinition(new java.io.FileInputStream(validDefinition))

      inside(enrichedDefinition) { case Success(json) =>
        val firstVersion = json("api")("versions")(0)
        firstVersion("access")("type").as[String] shouldBe "PRIVATE"
        firstVersion("access")("whitelistedApplicationIds").as[List[String]] should contain.inOrderOnly("f0e2611e-2f45-4326-8cd2-6eefebec77b7","cafebabe-2f45-4326-8cd2-6eefebec77b7")
      }
    }

    "return a Failure if unable to transform definition.json" in {
      documentationController.enrichDefinition(new java.io.FileInputStream(invalidDefinition)) should matchPattern { case Failure(_) => }
    }

    "return a success" when {
      "the documentation is able to be found" in {
        when(mockAppContext.mocked.environment).thenReturn(Environment(new File(".").getCanonicalFile, this.getClass.getClassLoader, Mode.Dev))
        val result = await(documentationController.documentation("1.0", "employment check")(FakeRequest()))

        status(result) shouldBe OK
      }

      "the definition is able to be found" in {
        val result = await(documentationController.definition()(FakeRequest()))

        status(result) shouldBe OK
      }
    }

    "return an error" when {
      "the documentation cannot be found" in {
        when(mockAppContext.mocked.environment).thenReturn(Environment(new File(".").getCanonicalFile, this.getClass.getClassLoader, Mode.Dev))
        val result = await(documentationController.documentation("1.0", "test file")(FakeRequest()))

        status(result) shouldBe NOT_FOUND
      }
      "the definition cannot be found" in {
        val result = await(documentationController.definition("TestFile.json")(FakeRequest()))

        status(result) shouldBe NOT_FOUND
      }
    }
  }
}


