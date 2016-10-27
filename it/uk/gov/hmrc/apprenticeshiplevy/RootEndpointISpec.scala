package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import org.scalatest.Matchers._

import org.scalacheck.Gen

import play.api.test.{FakeRequest, Helpers, RouteInvokers}
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Play
import play.api.libs.Crypto
import play.api.Play._
import views.html.helper

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class RootEndpointISpec extends WiremockFunSpec  {
  describe("Root Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { case (context) =>
      describe (s"should when calling ${localMicroserviceUrl}$context/") {
        describe (s"and backend systems not failing") {
          it (s"should return links for each empref") {
            sys.props.get("play.crypto.secret") match {
              case Some(_) => {
                // set up
                val responseBodyEnc = "03295379a9f614aa18fac872051d73cae3fdde4881518514800abbec6be43abe764292898a91e901945ec4f136d3cbf140edba699eec1dc38a53662fa3ee58545e1ffbffc8c86c6acd258bb2586a1af295cdfb37eeede2444ebe585749a83d9a9dfe9aa4155b8cc7f79a366a68024f583e470d99132c9764cba0e7678768f3b627afbd957b6e7097dc01b5c0bd87eeef3fc08afc0528d876537f46af5bfdd0577889a2f0205e26316a8e1debabf9606e0c56f9f6e7b0afcce5f072c4aea0dcae5c5ea15b7c33b532a002b2c8b1c82b050f6c73e0df4effcb200fe6bc1693144a7d88d1e1d258d033be183a916e3dbc7d1c69c860c05859f1591e009b557790e6a208817916078f1758c5f477c1868eb0"
                val response = Crypto.decryptAES(responseBodyEnc)
                //println(s"'${Crypto.encryptAES(response)}'")
                println(response)
                stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withBody(response)))
                val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

                // test
                val result = route(request).get

                // check
                status(result) shouldBe OK
                contentType(result) shouldBe Some("application/hal+json")
                val json = contentAsJson(result)
                (json \ "_links" \ "self" \ "href").as[String] shouldBe "/"
                (json \ "_links" \ "123/AB12345" \ "href").as[String] shouldBe "/epaye/123%2FAB12345"
              }
              case _ => fail("play.crypto.secret system property not set")
            }
          }
        }

        describe ("when errors occur") {
          it (s"HTTP 401") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(401).withStatusMessage("Not authorised.")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream4xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
        }

        describe ("when backend systems failing") {
          it (s"should throw IOException? when connection closed") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[java.io.IOException] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
          it (s"HTTP 500") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(500).withStatusMessage("Internal server error")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
          it (s"HTTP 503") {
            // set up
            stubFor(get(urlEqualTo("/auth/authority")).withId(uuid).willReturn(aResponse().withStatus(503).withStatusMessage("Backend systems failing")))
            val request = FakeRequest(GET, s"$context/").withHeaders("ACCEPT"->"application/vnd.hmrc.1.0+json")

            intercept[uk.gov.hmrc.play.http.Upstream5xxResponse] {
              // test
              val result = route(request).get

              // check
              contentType(result) shouldBe Some("application/json")
            }
          }
        }
      }
    }
  }
}