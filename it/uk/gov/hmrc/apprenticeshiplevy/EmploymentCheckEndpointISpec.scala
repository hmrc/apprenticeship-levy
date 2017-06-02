package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._
import views.html.helper

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatestplus.play._

@DoNotDiscover
class EmploymentCheckEndpointISpec extends WiremockFunSpec with ConfiguredServer  {
  describe("Employment Check Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/employed/<nino>") {
        describe ("with valid parameters") {
          it (s"?fromDate=2015-03-03&toDate=2015-06-30 should return 'employed'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/AA123456A?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"AA123456A","fromDate":"2016-03-03","toDate":"2016-06-30","employed":true}""")
          }

          it (s"?fromDate=2016-03-03&toDate=2016-06-30 should return 'not recognised'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/BB123456A?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            info(contentAsString(result))
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"code":"EPAYE_UNKNOWN","message":"The provided NINO or EMPREF was not recognised"}""")
          }

          it (s"?fromDate=2015-03-03&toDate=2015-06-30 should return 'not employed'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/EE123456A?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"EE123456A","fromDate":"2016-03-03","toDate":"2016-06-30","employed":false}""")
          }
        }

        describe ("with invalid paramters") {
          it (s"should return 400 when empref is badly formatted") {
            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val emprefs = for { empref <- genEmpref } yield empref

            forAll(emprefs) { (empref: String) =>
              whenever (!empref.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

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

          it (s"should return 400 when nino is badly formatted") {
            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val ninos = for { nino <- genNino } yield nino

            forAll(ninos) { (nino: String) =>
              whenever (!nino.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/444%2FAB12345/employed/${helper.urlEncode(nino)}?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

                // test
                val result = route(app, request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe 400
                contentType(result) shouldBe Some("application/json")
                contentAsString(result) should include ("""is in the wrong format. Should have a prefix (one of """)
              }
            }
          }

          Seq("fromDate", "toDate").foreach { case (param) =>
            it (s"should return 400 when $param param is invalid") {
              // set up
              WiremockService.notifier.testInformer = NullInformer.info
              val dates = for { str <- Gen.listOf(Gen.alphaNumChar) } yield str.mkString

              forAll(dates) { (date: String) =>
                whenever (!date.isEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/employed/RA123456C?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/employed/RA123456C?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders: _*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe 400
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include ("""date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date is yyyy-MM-dd and year is 2000 or later.""")
                }
              }
            }
          }

          it (s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/employed/RA123456C?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }

          it (s"should return 400 when DES returns 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error: Bad request"}""")
          }

          it (s"should return http status 401 when DES returns 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error: Not authorized"}""")
          }

          it (s"should return http status 403 when DES returns 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error: Forbidden"}""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error: Remotely closed"}""")
          }

          it (s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include ("DES not responding error: GET of 'http://localhost:8080/apprenticeship-levy/employers/777AB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30' timed out with message")
          }

          it (s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: Not found"}""")
          }

          it (s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/employed/RA123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error: Not found"}""")
          }
        }
      }
    }
  }
}
