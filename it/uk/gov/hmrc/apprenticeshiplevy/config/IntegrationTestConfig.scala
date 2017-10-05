package uk.gov.hmrc.apprenticeshiplevy.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.libs.json.Json
import org.scalatest.Informing
import com.github.tomakehurst.wiremock.common._
import play.api.libs.Crypto
import scala.io.Source
import java.io.File

trait IntegrationTestConfig {
  System.setProperty("logger.resource","logback-test.xml")

  def fileToStr(filename: String): String = Source.fromFile(new File(s"$filename")).getLines.mkString("\n")
  def aesKey: String = sys.props.get("play.crypto.secret").map(_.substring(0, 16)).getOrElse("")
  def verboseWiremockOutput: Boolean = sys.props.getOrElse("WIREMOCK_VERBOSE_OUTPUT", "false").toBoolean
  def stubPort = sys.props.getOrElse("WIREMOCK_PORT", "8080").toInt
  def stubHost = sys.props.getOrElse("WIREMOCK_HOST", "localhost")
  def stubConfigPath = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/resources")
  def resourcePath = sys.props.getOrElse("RESOURCE_PATH", "./it/resources")

  // val wireMockUrl = s"http://$stubHost:$stubPort"
  // apprenticeship-levy
  def test_host = sys.props.getOrElse("MICROSERVICE_HOST", "localhost")
  def test_port = sys.props.getOrElse("MICROSERVICE_PORT", "9001").toInt
  def localMicroserviceUrl = s"http://$test_host:$test_port"
  def microserviceUrl = sys.props.getOrElse("MICROSERVICE_URL", s"http://localhost:$test_port")

  val controllerSettings = Seq("LiveLevyDeclarationController",
                               "LiveRootController",
                               "LiveEmprefController",
                               "LiveFractionsController",
                               "LiveFractionsCalculationDateController",
                               "LiveEmploymentCheckController").map(a=>(s"controllers.uk.gov.hmrc.apprenticeshiplevy.controllers.live.${a}.needsAuditing","false"))

  def additionalConfiguration: Map[String, Any] = Map(
        "ws.timeout.request" -> "500",
        "ws.timeout.connection" -> "500",
        "http.port" -> test_port,
        "auditing.enabled" -> "false",
        "microservice.private-mode" -> "true",
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name",
        "microservice.metrics.graphite.enabled" -> "false",
        "Test.microservice.services.service-locator.host" -> stubHost,
        "Test.microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.host" -> stubHost,
        "microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.enabled" -> "true",
        "microservice.services.stub-auth.host" -> stubHost,
        "microservice.services.stub-auth.port" -> stubPort,
        "microservice.services.stub-auth.path" -> "",
        "microservice.services.stub-des.host" -> stubHost,
        "microservice.services.stub-des.port" -> stubPort,
        "microservice.services.stub-des.path" -> "",
        "microservice.services.des.host" -> stubHost,
        "microservice.services.des.port" -> stubPort,
        "microservice.services.des.path" -> "",
        "microservice.services.auth.host" -> stubHost,
        "microservice.services.auth.port" -> stubPort,
        "microservice.services.auth.path" -> "",
        "microservice.whitelisted-applications" -> "myappid1,myappid2"
        ) ++ Map(controllerSettings: _*)
}
