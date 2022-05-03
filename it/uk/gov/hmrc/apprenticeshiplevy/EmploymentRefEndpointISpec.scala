package uk.gov.hmrc.apprenticeshiplevy

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import org.scalatest.matchers.should.Matchers._
import org.scalatest._
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

//noinspection ScalaStyle
@DoNotDiscover
class EmploymentRefEndpointISpec
  extends WiremockFunSpec
    with ConfiguredServer {
  describe("Empref Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { context =>
      describe(s"should when calling $localMicroserviceUrl$context/epaye/<empref>") {
        describe(s"with valid parameters") {
          it("should return the declarations and fractions link for each empref") {
            val response =
              """{
                |  "allEnrolments": [{
                |    "key": "IR-PAYE",
                |    "identifiers": [
                |      { "key": "TaxOfficeNumber", "value": "123" },
                |      { "key": "TaxOfficeReference", "value": "AB12345" }
                |    ],
                |    "state": "Activated"
                |  }],
                |  "authProviderId": {
                |    "paClientId": "123"
                |  },
                |  "optionalCredentials": {
                |    "providerId": "123",
                |    "providerType": "paClientId"
                |  }
                |}""".stripMargin

            stubFor(post(urlEqualTo("/auth/authorise")).withId(uuid).willReturn(aResponse().withBody(response)))

            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES17").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 200
            contentType(result) shouldBe Some("application/hal+json")
            val json = contentAsJson(result)
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES17"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES17/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES17/declarations"
            (json \ "employer" \ "name" \ "nameLine1").as[String] shouldBe "KILL-JOY PLC THE FUNERAL"
            (json \ "communication" \ "name" \ "nameLine1").as[String] shouldBe "KILL-JOY PLC THE FUNERAL"
          }
        }

        describe("with invalid parameters") {
          it(s"when DES returns 400 should return 400") {
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 400
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          it(s"when DES returns unauthorized should return 401") {
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 401
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          it(s"when DES returns forbidden should return 403") {
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          it(s"when DES returns 404 should return 404") {
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_NOT_FOUND","message":"DES endpoint or EmpRef not found"}""")
          }
        }

        describe("when backend systems failing") {
          it(s"should return 503 when connection closed") {
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return 503 when response is empty") {
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return 408 when timed out") {
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include("DES not responding error")
          }

          it(s"should return 503 when DES returns 500") {
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          it(s"should return 503 when DES returns 503") {
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            status(result) shouldBe 503
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          it(s"should return the declarations and fractions link for each empref when employment details does not respond") {
            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES18").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            val json = contentAsJson(result)

            status(result) shouldBe 200
            contentType(result) shouldBe Some("application/hal+json")
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES18"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES18/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES18/declarations"
            (json \ "communication" \ "name" \ "nameLine1").as[String] shouldBe "KILL-JOY PLC THE FUNERAL"
            (json \ "employer").asOpt[String] shouldBe None
          }

          it(s"should return the declarations and fractions link for each empref when communication details does not respond") {
            val request = FakeRequest(GET, s"$context/epaye/840%2FMODES19").withHeaders(standardDesHeaders(): _*)

            val result = route(app, request).get

            val json = contentAsJson(result)

            status(result) shouldBe 200
            contentType(result) shouldBe Some("application/hal+json")
            (json \ "_links" \ "self" \ "href").as[String] shouldBe "/epaye/840%2FMODES19"
            (json \ "_links" \ "fractions" \ "href").as[String] shouldBe "/epaye/840%2FMODES19/fractions"
            (json \ "_links" \ "declarations" \ "href").as[String] shouldBe "/epaye/840%2FMODES19/declarations"
            (json \ "employer" \ "name" \ "nameLine1").as[String] shouldBe "KILL-JOY PLC THE FUNERAL"
            (json \ "communication").asOpt[String] shouldBe None
          }
        }
      }
    }
  }
}