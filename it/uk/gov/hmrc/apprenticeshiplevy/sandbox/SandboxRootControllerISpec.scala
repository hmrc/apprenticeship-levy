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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.SandboxRootController
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.DoNotDiscover
import scala.concurrent.Future

@DoNotDiscover
class SandboxRootControllerISpec extends UnitSpec with ScalaFutures with IntegrationPatience {

  "getting the root" should {
    "return links for each empref" in {
      val result = testController.root(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue
      result.header.status shouldBe OK
      val json = contentAsJson(result)

      (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
      (json \ "_links" \ "123/AB12345" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
    }
  }

  lazy val dummyAuthConnector = new AuthConnector {
    override def authBaseUrl: String = ???

    override def http: HttpGet = ???

    override def getEmprefs(implicit hc: HeaderCarrier): Future[Seq[String]] = Future.successful(Seq("123/AB12345"))
  }

  lazy val testController = new SandboxRootController {
    override def env: String = "Test"

    override def authConnector: AuthConnector = dummyAuthConnector
  }

}
