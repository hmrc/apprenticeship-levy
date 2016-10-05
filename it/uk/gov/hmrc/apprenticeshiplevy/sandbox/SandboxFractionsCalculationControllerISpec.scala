package uk.gov.hmrc.apprenticeshiplevy.sandbox

import play.api.test.FakeRequest
import play.api.test.Helpers._

import uk.gov.hmrc.play.test.UnitSpec

import org.scalatest.DoNotDiscover
import org.scalatest.Matchers._
import org.scalatest.prop._

import org.scalacheck.Gen

import play.api.libs.json.Json
import views.html.helper

@DoNotDiscover
class SandboxFractionsCalculationControllerISpec extends UnitSpec with GeneratorDrivenPropertyChecks {
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
  }
}