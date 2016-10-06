package uk.gov.hmrc.apprenticeshiplevy

import scala.io.Source
import scala.xml.XML._
import java.io.File

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.prop._
import org.scalatest.xml.XmlMatchers._

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

  def asXml(content: String): scala.xml.Elem = {
    loadString(content)
  }

  describe (s"Calling ${localMicroserviceUrl}/api/definition") {
    it (s"should return OK") {
      // set up
      val request = FakeRequest(GET, "/api/definition")

      // test
      val result = route(request).get

      // check
      status(result) shouldBe OK
    }

    it (s"should provide expected JSON API definition") {
      // set up
      val request = FakeRequest(GET, "/api/definition")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
    }
  }
}

@DoNotDiscover
class PublicDefinitionEndpointISpec
extends UnitSpec with GeneratorDrivenPropertyChecks with IntegrationTestConfig {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  s"Calling ${localMicroserviceUrl}/api/definition when private-mode setting is false API" should {
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