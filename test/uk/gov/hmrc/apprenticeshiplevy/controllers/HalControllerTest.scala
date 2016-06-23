package uk.gov.hmrc.apprenticeshiplevy.controllers

import java.net.URLEncoder

import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec

class HalControllerTest extends UnitSpec {

  val testController = new HalController {
    override def rootUrl: String = "/"

    override def authConnector: AuthConnector = ???

    override def emprefUrl(empref: String): String = s"""/epaye/${URLEncoder.encode(empref, "UTF-8")}"""
  }

  "transformEmprefs" should {
    "correctly generate json for emprefs" in {
      val hal = testController.transformEmpRefs(Seq("123/AB12345", "321/XY54321"))

      val json = Json.toJson(hal)

      (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
      (json \ "_links" \ "123/AB12345" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
      (json \ "_links" \ "321/XY54321" \ "href").as[String] shouldBe "/epaye/321%2FXY54321"

    }
  }

}
