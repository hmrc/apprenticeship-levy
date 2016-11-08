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

@DoNotDiscover
class EmploymentCheckEndpointISpec extends WiremockFunSpec  {
  describe("Employment Check Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/epaye/<empref>/employed/<nino>") {
        describe ("with valid parameters") {
          it (s"?fromDate=2015-03-03&toDate=2015-06-30 should return 'employed'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/AB123456A?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"AB123456A","fromDate":"2016-03-03","toDate":"2016-06-30","employed":true}""")
          }

          it (s"?fromDate=2016-03-03&toDate=2016-06-30 should return 'not recognised'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/ZZ123456C?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            info(contentAsString(result))
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"code":"EPAYE_NINO_UNKNOWN","message":"The provided NINO was not recognised"}""")
          }

          it (s"?fromDate=2015-03-03&toDate=2015-06-30 should return 'not employed'") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/employed/AB123456C?fromDate=2016-03-03&toDate=2016-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            val json = contentAsJson(result)
            json shouldBe Json.parse("""{"empref":"123/AB12345","nino":"AB123456C","fromDate":"2016-03-03","toDate":"2016-06-30","employed":false}""")
          }
        }

        describe ("with invalid paramters") {
          it (s"should return 404 when empref is unknown") {
            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val emprefs = for { empref <- genEmpref } yield empref

            forAll(emprefs) { (empref: String) =>
              whenever (!empref.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

                // test
                val result = route(request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe 404
                contentType(result) shouldBe Some("application/json")
                contentAsJson(result) shouldBe Json.parse("""{"code":"EPAYE_NINO_UNKNOWN","message":"The provided NINO was not recognised"}""")
              }
            }
          }

          it (s"should return 404 when nino is unknown") {
            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val ninos = for { nino <- genNino } yield nino

            forAll(ninos) { (nino: String) =>
              whenever (!nino.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/AB12345/employed/${helper.urlEncode(nino)}?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

                // test
                val result = route(request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe 404
                contentType(result) shouldBe Some("application/json")
                contentAsJson(result) shouldBe Json.parse("""{"code":"EPAYE_NINO_UNKNOWN","message":"The provided NINO was not recognised"}""")
              }
            }
          }

          Seq("fromDate", "toDate").foreach { case (param) =>
            it (s"should return 400 when $param param is invalid") {
              // set up
              WiremockService.notifier.testInformer = NullInformer.info
              val dates = for { str <- Gen.alphaStr } yield str

              forAll(dates) { (date: String) =>
                whenever (!date.isEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/AB12345/employed/QQ123456C?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/AB12345/employed/QQ123456C?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders: _*)

                  // test
                  val result = route(request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe 400
                  contentType(result) shouldBe Some("application/json")
                  contentAsJson(result) shouldBe Json.parse("""{"statusCode":400,"message":"date parameter is in the wrong format. Should be (yyyy-MM-dd)"}""")
                }
              }
            }
          }

          it (s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400/employed/QQ123456C?fromDate=2015-06-03&toDate=2015-03-30}").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"statusCode":400,"message":"date parameter is in the wrong format. Should be (yyyy-MM-dd)"}""")
          }

          it (s"should return 400 when DES returns 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"Bad request error: Not found"}""")
          }

          it (s"should return http status 401 when DES returns 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            val httpStatus = status(result)
            httpStatus shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES unauthorised error: Not authorized"}""")
          }

          it (s"should return http status 403 when DES returns 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES forbidden error: Forbidden"}""")
          }
        }

        describe ("when backend systems failing") {
          it (s"should return 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/malformed/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES connection error: Remotely Closed"}""")
          }

          it (s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/empty/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES connection error: Remotely Closed"}""")
          }

          it (s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/timeout/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES not responding error: GET of 'http://localhost:8080/apprenticeship-levy/employers/timeout/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30' timed out with message 'Request timed out to localhost/127.0.0.1:8080 of 500 ms'"}""")
          }

          it (s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES 5xx error: Not found"}""")
          }

          it (s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders(standardDesHeaders: _*)

            // test
            val result = route(request).get

            // check
            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR","message":"DES 5xx error: Not found"}""")
          }
        }
      }
    }
  }
}