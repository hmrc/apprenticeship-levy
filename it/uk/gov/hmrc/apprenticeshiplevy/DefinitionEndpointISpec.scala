package uk.gov.hmrc.apprenticeshiplevy

import scala.io.Source
import java.io.File

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.prop._

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class DefinitionEndpointISpec extends WiremockFunSpec  {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  describe (s"API Definition Endpoint (Private Mode)") {
    describe (s"should when calling ${localMicroserviceUrl}/api/definition") {
      describe (s"when definition file exists and private-mode is set to true") {
        it (s"return OK") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(request).get

          // check
          status(result) shouldBe OK
        }

        it (s"return expected JSON API definition") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(request).get

          // check
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
        }

        it (s"return definition with whitelisted applications") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(request).get

          // check
          contentType(result) shouldBe Some("application/json")
          val json = contentAsJson(result)
          val version1 = (json \ "api" \ "versions")(0)
          (version1 \ "access" \ "type").as[String] shouldBe "PRIVATE"
          (version1 \ "access" \ "whitelistedApplicationIds")(0).as[String] shouldBe "myappid"
        }
      }
    }
  }
}

@DoNotDiscover
class PublicDefinitionEndpointISpec extends UnitSpec with IntegrationTestConfig {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  "API Definition Endpoint (Public)" can {
    s"when calling ${localMicroserviceUrl}/api/definition" should {
      "when private-mode is set to false" must {
        "return definition without whitelisted-applications" in {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(request).get

          // check
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(asString("publicdefinition.json"))
        }
      }
    }
  }
}