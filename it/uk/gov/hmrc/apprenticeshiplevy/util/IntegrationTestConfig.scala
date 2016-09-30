package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.domain.ServiceLocatorRegistration
import org.scalatest.Informing
import com.github.tomakehurst.wiremock.common._


trait IntegrationTestConfig {
  def verboseWiremockOutput: Boolean = sys.props.getOrElse("WIREMOCK_VERBOSE_OUTPUT", "false").toBoolean
  def stubPort = sys.props.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "8080").toInt
  def stubHost = sys.props.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "localhost")
  def stubConfigPath = sys.props.getOrElse("WIREMOCK_SERVICE_RESPONSE_CONFIG", "./it/resources")
  def resourcePath = sys.props.getOrElse("RESOURCE_PATH", "./it/resources")

  // val wireMockUrl = s"http://$stubHost:$stubPort"
  def additionalConfiguration: Map[String, Any] = Map(
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name.service",
        "microservice.services.service-locator.host" -> stubHost,
        "microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.enabled" -> "true",
        "microservice.whitelisted-applications" -> "myappid")
  def port = sys.props.getOrElse("MICROSERVICE_PORT", "9001").toInt
  def localMicroserviceUrl = s"http://localhost:$port"
}