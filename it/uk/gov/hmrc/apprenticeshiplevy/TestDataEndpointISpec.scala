package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._
import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.Play._
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import org.scalatestplus.play._

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
    }
  }
}
