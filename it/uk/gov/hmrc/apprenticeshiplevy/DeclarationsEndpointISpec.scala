package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.joda.time.{LocalDate, LocalDateTime}

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._
import play.api.Play.current
import play.api.libs.ws.WS
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.api.{LevyDeclaration,PayrollPeriod}
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import org.scalatestplus.play._

@DoNotDiscover
class DeclarationsEndpointISpec extends WiremockFunSpec with IntegrationTestConfig with ConfiguredServer {
  describe("Declarations Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/declarations") {
        describe (s"with valid paramters") {
          it (s"should return levy declarations") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.size shouldBe 9
            info(declarations.mkString("\n"))
          }

          it (s"should return levy declarations where fromDate only specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12342/declarations?fromDate=2016-10-12").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12342"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.size shouldBe 2
            //info(declarations.mkString("\n"))
          }

          it (s"should return levy declarations where toDate only specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12343/declarations?toDate=2016-02-21").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12343"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.size shouldBe 5
            //info(declarations.mkString("\n"))
          }

          it (s"should return levy declarations where fromDate and toDate are specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12344/declarations?fromDate=2016-05-19&toDate=2016-05-21").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12344"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.size shouldBe 1
            //info(declarations.mkString("\n"))
          }

          it (s"should handle no payment period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(2) shouldBe LevyDeclaration(56774248741L,LocalDateTime.parse("2016-04-20T14:25:32.000"),None,None,None,Some(PayrollPeriod("16-17",8)),None,None,Some(true))
          }

          it (s"should handle inactive period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(7) shouldBe LevyDeclaration(6573215455L,LocalDateTime.parse("2016-04-20T14:25:32.000"),None,Some(LocalDate.parse("2016-08-06")),Some(LocalDate.parse("2016-11-05")),None,None,None,None)
          }

          it (s"should handle ceased trading") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(6) shouldBe LevyDeclaration(56774248742L,LocalDateTime.parse("2016-04-20T14:25:32.000"),Some(LocalDate.parse("2016-06-06")),None,None,None,None,None,None)
          }

          it (s"should handle levies") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(0) shouldBe LevyDeclaration(56774248743L,LocalDateTime.parse("2016-05-20T14:25:32.000"),None,None,None,Some(PayrollPeriod("16-17",2)),Some(98.64),Some(15000),None)
            declarations(3) shouldBe LevyDeclaration(56774248741L,LocalDateTime.parse("2016-04-20T14:25:32.000"),None,None,None,Some(PayrollPeriod("16-17",11)),Some(24.27),Some(15000),None)
            declarations(5) shouldBe LevyDeclaration(56774248742L,LocalDateTime.parse("2016-04-20T14:25:32.000"),None,None,None,Some(PayrollPeriod("16-17",11)),Some(24.27),Some(15000),None)
            declarations(8) shouldBe LevyDeclaration(6573215455L,LocalDateTime.parse("2016-04-20T14:25:32.000"),None,None,None,Some(PayrollPeriod("16-17",2)),Some(124.27),Some(15000),None)
          }
        }

        describe ("with invalid parameters") {
          Seq("fromDate", "toDate").foreach { case (param) =>
            it (s"should return 400 when $param param is invalid") {
              // set up
              val dates = for { str <- Gen.listOf(Gen.alphaNumChar) } yield str.mkString

              forAll(dates) { (date: String) =>
                whenever (!date.isEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/declarations?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders: _*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe 400
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include (""" date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date is yyyy-MM-dd and year is 2000 or later.""")
                }
              }
            }
          }

          it (s"should return 400 when empref is unknown") {
            // set up
            val emprefs = for { empref <- genEmpref } yield empref

            forAll(emprefs) { (empref: String) =>
              whenever (!empref.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/declarations").withHeaders(standardDesHeaders: _*)

                // test
                val result = route(app, request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe 400
                contentType(result) shouldBe Some("application/json")
                contentAsString(result) should include ("""is in the wrong format. Should be ^\\d{3}/[0-9A-Z]{1,10}$ and url encoded."""")
              }
            }
          }

          it (s"should return 400 when DES returns 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error: Bad request"}""")
          }

          it (s"should return http status 401 when DES returns 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result)
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error: Not Authorized"}""")
          }

          it (s"should return http status 403 when DES returns 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error: Forbidden"}""")
          }

          it (s"DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
          }

          it (s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include ("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error: GET of """)
          }

          it (s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: DES internal error"}""")
          }

          it (s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/declarations").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: Backend system error"}""")
          }
        }
      }
    }
  }
}