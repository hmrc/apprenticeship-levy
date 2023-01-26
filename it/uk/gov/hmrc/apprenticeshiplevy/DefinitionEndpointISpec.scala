package uk.gov.hmrc.apprenticeshiplevy

import java.io.File
import org.scalatest.matchers.should.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import uk.gov.hmrc.apprenticeshiplevy.util.AppLevyItUnitSpec

import scala.io.Source

@DoNotDiscover
class DefinitionEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
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
          val result = route(app, request).get

          // check
          status(result) shouldBe OK
        }

        it (s"return expected JSON API definition") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
        }

        it (s"return definition with whitelisted applications") {
          // set up
          val request = FakeRequest(GET, "/api/definition")

          // test
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")
          val json = contentAsJson(result)
          val version1 = (json \ "api" \ "versions")(0)
          (version1 \ "access" \ "type").as[String] shouldBe "PRIVATE"
          (version1 \ "access" \ "whitelistedApplicationIds")(0).as[String] shouldBe "myappid1"
        }
      }
    }
  }
}

@DoNotDiscover
class PublicDefinitionEndpointISpec extends AppLevyItUnitSpec with IntegrationTestConfig with ConfiguredServer {
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
          val result = route(app, request).get

          // check
          contentType(result) shouldBe Some("application/json")
          contentAsJson(result) shouldBe Json.parse(asString("publicdefinition.json"))
        }
      }
    }
  }
}
