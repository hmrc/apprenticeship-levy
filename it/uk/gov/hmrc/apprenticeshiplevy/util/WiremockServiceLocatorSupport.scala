package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.domain.ServiceLocatorRegistration

trait WiremockServiceLocatorSupport {
  lazy val wireMockUrl = s"http://$stubHost:$stubPort"
  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val stubPort = sys.env.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "11112").toInt
  val stubHost = "localhost"

  def regPayloadStringFor(serviceName: String, serviceUrl: String): String =
    Json.toJson(ServiceLocatorRegistration(serviceName, serviceUrl, Some(Map("third-party-api" -> "true")))).toString

  def startMockServer() = {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  def stopMockServer() = {
    wireMockServer.stop()
    wireMockServer.resetMappings()
  }

  def stubRegisterEndpoint(status: Int) = stubFor(post(urlMatching("/registration"))
    .willReturn(aResponse().withStatus(status)))
}
