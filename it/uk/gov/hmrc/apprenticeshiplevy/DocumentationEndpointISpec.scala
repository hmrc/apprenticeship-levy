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
import org.scalatestplus.play._

@DoNotDiscover
class DocumentationEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
  def asString(filename: String): String = {
    Source.fromFile(new File(s"${resourcePath}/data/expected/$filename")).getLines.mkString("\n")
  }

  def asXml(content: String): scala.xml.Elem = {
    loadString(content)
  }

  describe (s"API Documentation Endpoint") {
    describe (s"should provide RAML documentation") {
      val versions = Seq("1.0")
      versions.foreach { case (version) =>
        it (s"${localMicroserviceUrl}/api/conf/$version/application.raml is defined") {
          // set up
          val request = FakeRequest(GET, s"/api/conf/$version/application.raml")

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 200
          contentAsString(result).startsWith("#%RAML 1.0") shouldBe true
        }

        val definitionFile = new File(s"./public/api/conf/$version/application.raml")
        val includes = Source.fromFile(definitionFile).getLines.filter(_.contains("!include")).map((line)=>line.substring(line.indexOf("!include "))).toList.filterNot(_.matches(".*(errors|versioning).md")).toSet
        includes.zipWithIndex.foreach { case (include, i) =>
          it (s"and serve file for $include") {
            // set up
            val file = include.substring(9)
            val request = FakeRequest(GET, s"/api/conf/$version/$file")

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 200
          }
        }
      }
    }

    describe (s"should when calling ${localMicroserviceUrl}/api/documentation/<version>/<endpoint>") {
      describe (s"with valid parameters") {
        val definitionFile = new File("./public/api/definition.json")
        val definitionContents = Source.fromFile(definitionFile).getLines.mkString
        val definitionJson = Json.parse(definitionContents)
        val versions = (definitionJson \\ "version") map (_.as[String])
        versions.foreach { case (version) =>
          describe (s"when version is $version and endpoint is ") {
            val endpointNames = (definitionJson \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String].toLowerCase))
            versions.zip(endpointNames).flatMap { case (version, endpoint) =>
              endpoint.map(endpointName => (version, endpointName))
            }.filter(_._1 == version).foreach { case (v, endpointName) =>
              it (s"'$endpointName' should return expected documentation") {
                // set up
                val request = FakeRequest(GET, s"/api/documentation/$version/$endpointName")
                val expectedXml = asXml(asString(s"${endpointName}.xml"))

                // test
                val documentationResult = route(app, request).get
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

      describe (s"with invalid parameters") {
        it (s"should return 404 when documentation version doesn't exist") {
          // set up
          WiremockService.notifier.testInformer = NullInformer.info
          val urls = for { version <- Gen.choose(Int.MinValue, Int.MaxValue) } yield (s"/api/documentation/${version}/empref")

          forAll(urls) { (url: String) =>
            val request = FakeRequest(GET, url)

            // test
            val documentationResult = route(app, request).get
            val httpStatus = status(documentationResult)

            // check
            httpStatus shouldBe 404
          }
        }


        it (s"should return 404 when documentation endpoint doesn't exist") {
          // set up
          WiremockService.notifier.testInformer = NullInformer.info
          val endpoints = for { endpoint <- Gen.alphaStr } yield (endpoint)

          forAll(endpoints) { (endpoint: String) =>
            whenever (!endpoint.isEmpty && endpoint != "/") {
              val url = s"/api/documentation/1.0/${endpoint}"
              val request = FakeRequest(GET, url)

              // test
              val documentationResult = route(app, request).get
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