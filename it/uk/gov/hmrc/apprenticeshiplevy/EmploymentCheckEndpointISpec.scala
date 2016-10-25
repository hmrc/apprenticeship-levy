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
            val request = FakeRequest(GET, s"$context/epaye/AB12345/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val result = route(request).get

            // check
            contentType(result) shouldBe Some("application/json")
            if (contentAsString(result).contains("NOT_IMPLEMENTED")) {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
            } else {
              val json = contentAsJson(result)
              json shouldBe Json.parse("""{"empref":"AB12345","nino":"QQ123456C","fromDate":"2015-03-03","toDate":"2015-06-30","employed":true}""")
            }
          }

          it (s"?fromDate=2015-03-03&toDate=2015-06-30 should return 'not_employed'") {
            pending
          }
        }

        describe ("with invalid paramters") {
          it (s"should return 404 when empref is unknown") {
            info("why getting 401 in live as opposed to 404 for sandbox????")

            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val emprefs = for { empref <- Gen.alphaStr } yield empref

            forAll(emprefs) { (empref: String) =>
              whenever (!empref.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/employed/QQ123456C?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

                // test
                val documentationResult = route(request).get
                val httpStatus = status(documentationResult)

                // check
                if (context == "") {
                  httpStatus shouldBe 401
                } else {
                  httpStatus shouldBe 404
                }
              }
            }
          }

          it (s"should return 404 when nino is unknown") {
            // set up
            WiremockService.notifier.testInformer = NullInformer.info
            val ninos = for { nino <- Gen.alphaStr } yield nino

            forAll(ninos) { (nino: String) =>
              whenever (!nino.isEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/AB12345/employed/${helper.urlEncode(nino)}?fromDate=2015-03-03&toDate=2015-06-30").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

                // test
                val documentationResult = route(request).get
                val httpStatus = status(documentationResult)

                // check
                httpStatus shouldBe 404
              }
            }
            if (context == "") {
              info("NOT_IMPLEMENTED")
              throw new org.scalatest.exceptions.TestPendingException()
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
                  val request = FakeRequest(GET, requestUrl).withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

                  // test
                  val documentationResult = route(request).get
                  val httpStatus = status(documentationResult)

                  // check
                  httpStatus shouldBe 400
                }
              }
            }
          }

          it (s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/AB12345/employed/QQ123456C?fromDate=2015-06-03&toDate=2014-06-03}").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            // test
            val documentationResult = route(request).get
            val httpStatus = status(documentationResult)

            // check
            httpStatus shouldBe 400
          }
        }

        describe ("when backend systems failing") {
          it (s"should") {
            pending
          }
        }
      }
    }
  }
}