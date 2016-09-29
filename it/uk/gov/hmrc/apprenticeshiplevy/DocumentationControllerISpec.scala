package uk.gov.hmrc.apprenticeshiplevy

import scala.io.Source
import scala.xml.XML._
import java.io.File

import org.scalatest.{FunSpec, DoNotDiscover, Status, Args}
import org.scalatest.Matchers._
import org.scalatest.xml.XmlMatchers._

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._

import uk.gov.hmrc.apprenticeshiplevy.util.IntegrationTestConfig

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
      contentAsJson(result) shouldBe Json.parse(asString("definition.json"))
    }

    it (s"should return not found when documentation version doesn't exist") {
      // set up
      val request = FakeRequest(GET, "/api/documentation/3131/empref")

      // test
      val documentationResult = route(request).get
      val httpStatus = status(documentationResult)

      // check
      httpStatus shouldBe 404
    }


    it (s"should return not found when documentation endpoint doesn't exist") {
      // set up
      val request = FakeRequest(GET, "/api/documentation/1.0/someapiendpoint")

      // test
      val documentationResult = route(request).get
      val httpStatus = status(documentationResult)

      // check
      httpStatus shouldBe 404
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

            // check
            httpStatus shouldBe OK
            xml should beXml (expectedXml, true)
          }
        }
      }
    }
    super.run(testName, args)
  }
}