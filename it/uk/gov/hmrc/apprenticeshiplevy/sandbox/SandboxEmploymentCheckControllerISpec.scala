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
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

@DoNotDiscover
class SandboxEmploymentCheckControllerISpec extends UnitSpec {
  "Employment check" should {
    "return 'employed' when valid request is made" in {
      // set up
      val request = FakeRequest(GET, "/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse("""{"empref":"AB12345","nino":"QQ123456C","fromDate":"2015-03-03","toDate":"2015-06-30","employed":true}""")
    }
  }
}