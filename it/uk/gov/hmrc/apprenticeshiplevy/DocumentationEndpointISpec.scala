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
class DocumentationEndpointISpec extends WiremockFunSpec  {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  def asXml(content: String): scala.xml.Elem = {
    loadString(content)
  }

  describe (s"API Documentation") {
    describe (s"when called with valid paramters") {
      val definitionFile = new File("./public/api/definition.json")
      val definitionContents = Source.fromFile(definitionFile).getLines.mkString
      val definitionJson = Json.parse(definitionContents)
      val versions = (definitionJson \\ "version") map (_.as[String])
      versions.foreach { case (version) =>
          describe (s"Calling ${localMicroserviceUrl}/api/documentation/$version") {
            val endpointNames = (definitionJson \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String].toLowerCase))
            versions.zip(endpointNames).flatMap { case (version, endpoint) =>
              endpoint.map(endpointName => (version, endpointName))
            }.filter(_._1 == version).foreach { case (v, endpointName) =>
              it (s"/$endpointName should return expected documentation") {
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
    }

    describe (s"when called with invalid paramters") {
      describe (s"Calling ${localMicroserviceUrl}/api/documentation/<version>/<endpoint-name>") {
        it (s"return 404 when documentation version doesn't exist") {
          // set up
          WiremockService.notifier.testInformer = NullInformer.info
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


        it (s"return 404 when documentation endpoint doesn't exist") {
          // set up
          WiremockService.notifier.testInformer = NullInformer.info
          val endpoints = for { endpoint <- Gen.alphaStr } yield (endpoint)

          forAll(endpoints) { (endpoint: String) =>
            whenever (!endpoint.isEmpty && endpoint != "/") {
              val url = s"/api/documentation/1.0/${endpoint}"
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
    }
  }
}