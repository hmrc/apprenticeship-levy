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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec

class FinalSubmissionSpec extends AppLevyUnitSpec {

  val ceaseDate: LocalDate = LocalDate.parse("2020-01-01")

  val combinedFinalSubmissionModel: CombinedFinalSubmission = CombinedFinalSubmission(None, Some(ceaseDate), Some(true))
  val schemeCeasedModel: SchemeCeased = SchemeCeased(schemeCeased = true, ceaseDate, Some(true))
  val lastSubmissionModel: LastSubmission = LastSubmission(true)

  val combinedFinalSubmissionJson: JsValue = Json.parse(
    """
      |{
      |   "schemeCeased": null,
      |   "schemeCeasedDate": "2020-01-01",
      |   "forYear": true
      |}
      |""".stripMargin)

  val combinedFinalSubmissionJsonReads: JsValue = Json.parse(
    """
      |{
      |   "schemeCeasedDate": "2020-01-01",
      |   "forYear": true
      |}
      |""".stripMargin)

  val schemeCeasedJson: JsValue = Json.parse(
    """
      |{
      |   "schemeCeased": true,
      |   "schemeCeasedDate": "2020-01-01",
      |   "forYear": true
      |}
      |""".stripMargin)

  val lastSubmissionJson: JsValue = Json.parse(
    """
      |{
      |   "forYear": true
      |}
      |""".stripMargin)

  val lastSubmissionJsonNull: JsValue = Json.parse(
    """
      |{
      |   "schemeCeased": null,
      |   "schemeCeasedDate": null,
      |   "forYear": true
      |}
      |""".stripMargin)

  "FinalSubmission" should {
    "read the json and return a combinedFinalSubmission model" when {
      "inputting a combinedFinalSubmission json" in {
        combinedFinalSubmissionJsonReads.as[FinalSubmission] shouldBe combinedFinalSubmissionModel
      }
    }
    "read the json and return a schemeCeased model" when {
      "inputting a combinedFinalSubmission json" in {
        schemeCeasedJson.as[FinalSubmission] shouldBe schemeCeasedModel
      }
    }
    "read the json and return a lastSubmission model" when {
      "inputting a combinedFinalSubmission json" in {
        lastSubmissionJson.as[FinalSubmission] shouldBe lastSubmissionModel
      }
    }

    "write to a json" when {
      "inputting a combinedFinalSubmission model" in {
        Json.toJson[FinalSubmission](combinedFinalSubmissionModel) shouldBe combinedFinalSubmissionJson
      }
      "inputting a schemeCeasedModel model" in {
        Json.toJson[FinalSubmission](schemeCeasedModel) shouldBe schemeCeasedJson
      }
      "inputting a lastSubmissionModel model" in {
        Json.toJson[FinalSubmission](lastSubmissionModel) shouldBe lastSubmissionJsonNull
      }
    }
  }
}
