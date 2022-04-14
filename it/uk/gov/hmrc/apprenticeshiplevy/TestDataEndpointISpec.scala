package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.test.FakeRequest
import play.api.test.Helpers._

@DoNotDiscover
class TestDataEndpointISpec extends WiremockFunSpec with ConfiguredServer {
  describe("Sandbox Test Data Endpoint") {
    describe ("authorise/read") {
      it ("should return Ok") {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/authorise/read")
                      .withHeaders(standardDesHeaders: _*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe 200
      }
    }

    describe ("/sandbox/data/...") {
      it ("should return Ok where json file exists") {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
                      .withHeaders(standardDesHeaders: _*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe 200
        contentAsString(result) should include ("/paye/employer/840/MODES17/designatory-details/employer")
      }

      it ("should return NotFound where json file does not exists") {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/zxy")
                      .withHeaders(standardDesHeaders: _*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe 404
      }

      it ("should return Ok where json file does not have json body") {
        // set up
        val request = FakeRequest(GET, "/sandbox/data/empty")
                      .withHeaders(standardDesHeaders: _*)

        // test
        val result = route(app, request).get

        // check
        status(result) shouldBe 200
      }

      describe("should return support OVERRIDE_EMPREF") {
        it ("and return json file where it exists") {
          // set up
          val headers = standardDesHeaders :+ (("OVERRIDE_EMPREF"->"840/MODES17"))
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/000/ABC/designatory-details")
                        .withHeaders(headers: _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 200
          contentAsString(result) should include ("/paye/employer/840/MODES17/designatory-details/employer")
        }

        it ("and return NotFound where json file does not exist") {
          // set up
          val headers = standardDesHeaders :+ (("OVERRIDE_EMPREF"->"ZZZ%2FJKLJLJL"))
          val request = FakeRequest(GET, "/sandbox/data/paye/employer/840/MODES17/designatory-details")
                        .withHeaders(headers: _*)

          // test
          val result = route(app, request).get

          // check
          status(result) shouldBe 404
        }
      }
    }
  }
}
