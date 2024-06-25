/*
 * Copyright 2023 HM Revenue & Customs
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

package test.uk.gov.hmrc.apprenticeshiplevy

import java.io.File
import javax.xml.parsers.{SAXParser, SAXParserFactory}
import org.scalacheck.Gen
import org.scalatest.DoNotDiscover
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.FakeRequest
import play.api.test.Helpers._
import test.uk.gov.hmrc.apprenticeshiplevy.util._

import scala.io.{BufferedSource, Source}
import scala.util.Using
import scala.xml.XML._

@DoNotDiscover
class DocumentationEndpointISpec extends WiremockFunSpec with ConfiguredServer with ScalaCheckPropertyChecks {
  def asString(filename: String): String = {
    val fileBuffer: BufferedSource = Source.fromFile(s"$resourcePath/data/expected/$filename")

    Using(fileBuffer) {
      file => file.getLines().mkString("\n")
    }.get
  }

  def asXml(content: String): scala.xml.Elem = {
    withSAXParser(secureSAXParser).loadString(content)
  }

  describe (s"API Documentation Endpoint") {
    describe (s"should provide YAML documentation") {
      val versions = Seq("1.0")
      versions.foreach { version =>
        it (s"$localMicroserviceUrl/api/conf/$version/application.yaml is defined") {
          // set up
          val request = FakeRequest(GET, s"/api/conf/$version/application.yaml")

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 200
          contentAsString(result).startsWith("openapi: 3.0.3") shouldBe true
        }

        val definitionFile = new File(s"./public/api/conf/$version/application.yaml")
        val fileBuffer: BufferedSource = Source.fromFile(definitionFile)

        val includes = Using(fileBuffer) {
          file => file.getLines().filter(_.contains("!include")).map(line=>line.substring(line.indexOf("!include "))).toList.filterNot(_.matches(".*(errors|versioning).md")).toSet
        }.get

        includes.zipWithIndex.foreach { case (include, _) =>
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

    describe (s"should when calling $localMicroserviceUrl/api/definition") {
      it (s"should have a correct white list configured") {
        // set up
        val request = FakeRequest(GET, s"/api/definition")

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe 200
        contentAsString(result) should include ("""["myappid1","myappid2"]""")
      }
    }

    describe (s"should when calling $localMicroserviceUrl/api/documentation/<version>/<endpoint>") {
      /* DEPRECATED
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
      }*/

      describe (s"with invalid parameters") {
        it (s"should return 404 when documentation version doesn't exist") {
          // set up
          WiremockService.notifier.testInformer = NullInformer.info
          val urls = for { version <- Gen.choose(Int.MinValue, Int.MaxValue) } yield s"/api/documentation/$version/empref"

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
          val endpoints = for { endpoint <- Gen.alphaStr } yield endpoint

          forAll(endpoints) { (endpoint: String) =>
            whenever (endpoint.nonEmpty && endpoint != "/") {
              val url = s"/api/documentation/1.0/$endpoint"
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

  def secureSAXParser: SAXParser = {
    val saxParserFactory = SAXParserFactory.newInstance()
    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    saxParserFactory.newSAXParser()
  }
}
