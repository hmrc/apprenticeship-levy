/*
 * Copyright 2016 HM Revenue & Customs
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

import org.scalatest.Inside
import play.api.Play
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.{Failure, Success}

class DocumentationControllerSpec extends UnitSpec with Inside {

  val validDefinition = getClass.getResourceAsStream("/validDefinition.json")
  val invalidDefinition = getClass.getResourceAsStream("/invalidDefinition.json")

  "DocumentationController" should {
    "add whitelist information correctly" in new TestDocumentationController {
      val enrichedDefinition = enrichDefinition(validDefinition)

      inside(enrichedDefinition) { case Success(json) =>
        val firstVersion = (json \ "api" \ "versions")(0)
        (firstVersion \ "access" \ "type").as[String] shouldBe "PRIVATE"
        (firstVersion \ "access" \ "whitelistedApplicationIds").as[List[String]] shouldBe List("f0e2611e-2f45-4326-8cd2-6eefebec77b7")
      }
    }

    "return a Failure if unable to transform definition.json" in new TestDocumentationController {
      enrichDefinition(invalidDefinition) should matchPattern { case Failure(_) => }
    }
  }
}

class TestDocumentationController extends DocumentationController {
  override implicit lazy val current = Play.current
}
