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
  def stubPort = sys.props.getOrElse("WIREMOCK_PORT", "8080").toInt
  def stubHost = sys.props.getOrElse("WIREMOCK_HOST", "localhost")
  def stubConfigPath = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/resources")
  def resourcePath = sys.props.getOrElse("RESOURCE_PATH", "./it/resources")

  // val wireMockUrl = s"http://$stubHost:$stubPort"
  // apprenticeship-levy
  def host = sys.props.getOrElse("MICROSERVICE_HOST", "localhost")
  def port = sys.props.getOrElse("MICROSERVICE_PORT", "9001").toInt
  def localMicroserviceUrl = s"http://$host:$port"
  def microserviceUrl = sys.props.getOrElse("MICROSERVICE_URL", s"http://localhost:$port")
  def additionalConfiguration: Map[String, Any] = Map(
        "http.port" -> port,
        "logger.root" -> "OFF",
        "logger.play" -> "OFF",
        "logger.application" -> "OFF",
        "logger.connector" -> "OFF",
        "auditing.enabled" -> "false",
        "microservice.private-mode" -> "true",
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name.service",
        "microservice.services.service-locator.host" -> stubHost,
        "microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.enabled" -> "true",
        "microservice.services.stub-auth.host" -> stubHost,
        "microservice.services.stub-auth.port" -> stubPort,
        "microservice.services.stub-rti.host" -> stubHost,
        "microservice.services.stub-rti.port" -> stubPort,
        "microservice.services.stub-edh.host" -> stubHost,
        "microservice.services.stub-edh.port" -> stubPort,
        "microservice.services.rti.host" -> stubHost,
        "microservice.services.rti.port" -> stubPort,
        "microservice.services.auth.host" -> stubHost,
        "microservice.services.auth.port" -> stubPort,
        "microservice.whitelisted-applications" -> "myappid")
}
