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

import uk.gov.hmrc.apprenticeshiplevy.util.{IntegrationTestConfig, PlayService}
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class DocumentationControllerISpec extends FunSpec with IntegrationTestConfig {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  def asXml(content: String): scala.xml.Elem = {
    loadString(content)
  }

  describe ("Documentation Controller") {
    it (s"should provide definition endpoint") {
      // set up
      val request = FakeRequest(GET, "/api/definition")

      // test
      val result = route(request).get

      // check
      status(result) shouldBe OK
    }

    it (s"should provide expected API definition") {
      // set up
      val request = FakeRequest(GET, "/api/definition")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
    }
  }

  override def run(testName: Option[String], args: Args): Status = {
    val definitionFile = Play.current.getFile("public/api/definition.json")
    val definitionContents = Source.fromFile(definitionFile).getLines.mkString
    val definitionJson = Json.parse(definitionContents)
    val versions = (definitionJson \\ "version") map (_.as[String])
    versions.foreach { case (version) =>
      describe (s"Documentation Controller With Version ${version}") {
        val endpointNames = (definitionJson \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String].toLowerCase))
        versions.zip(endpointNames).flatMap { case (version, endpoint) =>
          endpoint.map(endpointName => (version, endpointName))
        }.filter(_._1 == version).foreach { case (v, endpointName) =>
          it (s"should return expected documentation for ${endpointName}") {
            // set up
            val request = FakeRequest(GET, s"/api/documentation/$version/$endpointName")
            val expectedXml = asXml(asString(s"${endpointName}.xml"))

            // test
            val documentationResult = route(request).get
            val httpStatus = status(documentationResult)
            val xml = asXml(contentAsString(documentationResult))
            val contenttype = contentType(documentationResult)

            // check
            httpStatus shouldBe OK
            contenttype shouldBe Some("application/xml")
            xml should beXml (expectedXml, true)
          }
        }
      }
    }
    super.run(testName, args)
  }
}

class DocumentationControllerAlternateConfigISpec extends UnitSpec with IntegrationTestConfig with GeneratorDrivenPropertyChecks {
  val altConfigPlayService = new PlayService() {
    override def additionalConfiguration: Map[String, Any] = Map(
      "microservice.private-mode" -> "false",
      "appName" -> "application-name",
      "appUrl" -> "http://microservice-name.service",
      "microservice.services.service-locator.host" -> stubHost,
      "microservice.services.service-locator.port" -> stubPort,
      "microservice.services.service-locator.enabled" -> "true",
      "microservice.whitelisted-applications" -> "myappid")
  }

  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  "when private-mode setting is false API" should {
    "return definition without whitelisted-applications" in {
      // set up
      altConfigPlayService.start()
      val request = FakeRequest(GET, "/api/definition")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse(asString("publicdefinition.json"))
      altConfigPlayService.stop()
    }

    "should return not found when documentation version doesn't exist" in {
      // set up
      val urls = for { version <- Gen.choose(Int.MinValue, Int.MaxValue) } yield (s"/api/documentation/${version}/empref")

      forAll(urls) { (url: String) =>
        val request = FakeRequest(GET, url)

        // test
        val documentationResult = route(request).get
        val httpStatus = status(documentationResult)

        // check
        httpStatus shouldBe 404
      }
    }


    "should return not found when documentation endpoint doesn't exist" {
      // set up
      val urlss = for { endpoint <- Gen.alphaStr } yield (s"/api/documentation/1.0/${endpoint}")

      forAll(urlss) { (url: String) =>
        val request = FakeRequest(GET, url)

        // test
        val documentationResult = route(request).get
        val httpStatus = status(documentationResult)

        // check
        httpStatus shouldBe 404
      }
    }
  }
}