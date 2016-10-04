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

package uk.gov.hmrc.apprenticeshiplevy.sandbox

import play.api.test.FakeRequest
import play.api.test.Helpers._

import uk.gov.hmrc.play.test.UnitSpec

import org.scalatest.DoNotDiscover
import org.scalatest.Matchers._
import org.scalatest.prop._

import org.scalacheck.Gen

import play.api.libs.json.Json
import views.html.helper

@DoNotDiscover
class SandboxEmploymentCheckControllerISpec extends UnitSpec with GeneratorDrivenPropertyChecks {
  "Employment check" should {
    "return 'employed' when valid request is made to sandbox" in {
      // set up
      val request = FakeRequest(GET, "/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse("""{"empref":"AB12345","nino":"QQ123456C","fromDate":"2015-03-03","toDate":"2015-06-30","employed":true}""")
    }
    
    "return not found when empref doesn't exist" in {
      // set up
      val emprefs = for { empref <- Gen.alphaStr } yield empref

      forAll(emprefs) { (empref: String) =>
        whenever (!empref.isEmpty) {
          val request = FakeRequest(GET, s"/sandbox/epaye/${helper.urlEncode(empref)}/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

          // test
          val documentationResult = route(request).get
          val httpStatus = status(documentationResult)

          // check
          httpStatus shouldBe 404
        }
      }
    }
        
    "return not found when nino doesn't exist" in {
      // set up
      val ninos = for { nino <- Gen.alphaStr } yield nino

      forAll(ninos) { (nino: String) =>
        whenever (!nino.isEmpty) {
          val request = FakeRequest(GET, s"/sandbox/epaye/AB12345/employed/${helper.urlEncode(nino)}?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

          // test
          val documentationResult = route(request).get
          val httpStatus = status(documentationResult)

          // check
          httpStatus shouldBe 404
        }
      }
    }
    
    "return bad request when from date is invalid" in {
      // set up
      val fromDates = for { str <- Gen.alphaStr } yield str

      forAll(fromDates) { (fromDate: String) =>
        whenever (!fromDate.isEmpty) {
          val request = FakeRequest(GET, s"/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=${helper.urlEncode(fromDate)}&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

          // test
          val documentationResult = route(request).get
          val httpStatus = status(documentationResult)

          // check
          httpStatus shouldBe 400
        }
      }
    }

    "return bad request when to date is invalid" in {
      // set up
      val toDates = for { str <- Gen.alphaStr } yield str

      forAll(toDates) { (toDate: String) =>
        whenever (!toDate.isEmpty) {
          val request = FakeRequest(GET, s"/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=2015-06-03&toDate=${helper.urlEncode(toDate)}").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

          // test
          val documentationResult = route(request).get
          val httpStatus = status(documentationResult)

          // check
          httpStatus shouldBe 400
        }
      }
    }
    
    "return bad request when to date is before from date" in {
      // set up
      val request = FakeRequest(GET, s"/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=2015-06-03&toDate=2014-06-03}").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

      // test
      val documentationResult = route(request).get
      val httpStatus = status(documentationResult)

      // check
      httpStatus shouldBe 400
    }
  }
}
