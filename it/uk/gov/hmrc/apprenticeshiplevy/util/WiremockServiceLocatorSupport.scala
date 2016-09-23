package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.domain.ServiceLocatorRegistration
import org.scalatest.Informing
import com.github.tomakehurst.wiremock.common._

trait WiremockServiceLocatorSupport {
  scalatest: Informing =>

  protected def verboseWiremockOutput: Boolean = sys.env.getOrElse("WIREMOCK_VERBOSE_OUTPUT", "false").toBoolean
  val stubPort = sys.env.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "8080").toInt
  val stubHost = "localhost"
  val stubConfigPath = sys.env.getOrElse("WIREMOCK_SERVICE_RESPONSE_CONFIG", "./it/resources")

  lazy val wireMockUrl = s"http://$stubHost:$stubPort"

  scalatest.info(s"Configuring wire mock server to listen on $stubHost:$stubPort using responses configured in $stubConfigPath")
  lazy val wireMockServer = new WireMockServer(wireMockConfig.notifier(WiremockTestInformerNotifier(scalatest.info, verboseWiremockOutput)).usingFilesUnderDirectory(stubConfigPath).port(stubPort).bindAddress(stubHost))

  def regPayloadStringFor(serviceName: String, serviceUrl: String): String =
    Json.toJson(ServiceLocatorRegistration(serviceName, serviceUrl, Some(Map("third-party-api" -> "true")))).toString

  def startMockServer() = {
    wireMockServer.start()
  }

  def stopMockServer() = {
    wireMockServer.stop()
  }
}
