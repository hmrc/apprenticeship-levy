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
              case Some(secret) => {
                // set up
                val key = secret.substring(0, 16)
                val responseBodyEnc = "1610320a0447208bb56cb5a4f4133eca30573a9d8c67302e96a52dcb08e75402b92289dee806cb86704c1c3d7af9e18ed2b6818a9dc83519f192c9a0da5d9966653a136035525708645d74fe5d4aec48a330e2a7de150823508983073f525ab07a0b694c63b4aa2a34764dd591ba722f585a237141faaab6c23029823b06b6dd4f4590e9f36e2ae21b49cac109de908e08dbfaa2d6c9afab9c117d636c004d582d35c61004fbb63fe2f7a7e2dfe1d6c61cd9b298afa7b6865459e4cddf73faff511b6260b6741ac542b15c0172629c86fc1f66359513068fb0b40e5aceef36164639bd3de25ab6aa9151b0d357d85f356ae18b5098dc79eb0284197763c5ae3deafcd52b81fa8336137580eceb085a9e"
                val response = Crypto.decryptAES(responseBodyEnc,key)
                //println(s"'${Crypto.encryptAES(response, key)}'")
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