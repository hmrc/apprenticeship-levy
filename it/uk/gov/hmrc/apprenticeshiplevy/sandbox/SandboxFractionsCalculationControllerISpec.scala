package uk.gov.hmrc.apprenticeshiplevy.sandbox

import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.Json
import views.html.helper

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.WiremockSpec
import uk.gov.hmrc.apprenticeshiplevy.util.WiremockService

import org.scalatest.DoNotDiscover
import org.scalatest.Matchers._
import org.scalatest.prop._
import org.scalatest._
import org.scalatest.Assertions._

import org.scalacheck.Gen

import collection.JavaConverters._

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

@DoNotDiscover
class SandboxFractionsCalculationControllerISpec extends UnitSpec with GeneratorDrivenPropertyChecks with WiremockSpec {
  "Fraction Calculation Controller" should {
    "return fraction calculation date from EDH" in {
      // set up
      val request = FakeRequest(GET, "/sandbox/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

      // test
      val result = route(request).get

      // check
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.parse(""""2016-06-19"""")
    }

    "return fraction calculation date from EDH bad response" in {
      // set up
      WireMock.reset()
      stubFor(get(urlEqualTo("/fraction-calculation-date")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
      val request = FakeRequest(GET, "/sandbox/fraction-calculation-date").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

      intercept[java.io.IOException] {
        // test
          val result = route(request).get

        // check
        contentType(result) shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.parse(""""2016-06-19"""")
      }
      //WireMock.resetToDefault()
      WiremockService.wireMockServer.resetToDefaultMappings()
    }
  }
}