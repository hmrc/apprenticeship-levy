package uk.gov.hmrc.apprenticeshiplevy

import java.util.UUID._
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Logging
import play.api.inject.guice._
import play.api.{Application, Mode}
import uk.gov.hmrc.apprenticeshiplevy.config._
import uk.gov.hmrc.apprenticeshiplevy.util._

import scala.util.Try

class IntegrationTestsSuite
  extends Suites(
    new ConfigurationISpec,
    new DeclarationsEndpointISpec,
    new DefinitionEndpointISpec,
    new DocumentationEndpointISpec,
    new EmploymentCheckEndpointISpec,
    new EmploymentRefEndpointISpec,
    new FractionsEndpointISpec,
    new FractionsCalculationDateEndpointISpec,
    new RootEndpointISpec,
    new TestDataEndpointISpec
  ) with BeforeAndAfterAllConfigMap
    with IntegrationTestConfig
    with GuiceOneServerPerSuite
    with AppLevyUnitSpec
    with Logging {

  WiremockService.start()
  override implicit lazy final val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .in(Mode.Test)
    .build()

  lazy val auuid1 = randomUUID()
  lazy val auuid2 = randomUUID()
  lazy val auuid3 = randomUUID()
  lazy val auuid4 = randomUUID()
  lazy val auuid5 = randomUUID()

  override def beforeAll(cm: ConfigMap) {
    System.err.println("Starting Play...")

    sys.props.get("play.http.secret.key") match {
      case Some(_) => logger.info(s"play.http.secret.key system property set.")
      case _ => logger.warn(s"play.http.secret.key system property not set. Tests will fail.")
    }

    Try("/authorise/read/epaye/AB12345?confidenceLevel=50&privilegedAccess=read:apprenticeship-levy").foreach { validReadURL1 =>
      stubFor(get(urlEqualTo(validReadURL1)).withId(auuid1).atPriority(1).willReturn(aResponse().withStatus(200)))
    }

    Try("/authorise/read/epaye/123%2FAB12345?confidenceLevel=50&privilegedAccess=read:apprenticeship-levy").foreach { validReadURL2 =>
      stubFor(get(urlEqualTo(validReadURL2)).withId(auuid2).atPriority(1).willReturn(aResponse().withStatus(200)))
    }

    Try("/authorise/read/epaye/malformed?confidenceLevel=50&privilegedAccess=read:apprenticeship-levy").foreach { faultURL1 =>
      stubFor(get(urlEqualTo(faultURL1)).withId(auuid3).atPriority(3).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
    }

    Try("/authorise/read/epaye/(400|401|403|404|500|503|empty|malformed|timeout)%2FAB12345\\?confidenceLevel=50&privilegedAccess=read:apprenticeship-levy").foreach { invalidReadURL1 =>
      stubFor(get(urlMatching(invalidReadURL1)).withId(auuid4).atPriority(1).willReturn(aResponse().withStatus(200)))
    }

    Try("/authorise/read/epaye/(.*)\\?confidenceLevel=50&privilegedAccess=read:apprenticeship-levy").foreach { validRead =>
      stubFor(get(urlMatching(validRead)).withId(auuid5).atPriority(1).willReturn(aResponse().withStatus(200)))
    }
  }

  override def afterAll(cm: ConfigMap) {
    WiremockService.stop()
  }
}

class NoWiremockIntegrationTestsSuite
  extends Suites(
    new PublicDefinitionEndpointISpec
  ) with BeforeAndAfterAllConfigMap
    with IntegrationTestConfig
    with GuiceOneServerPerSuite
    with AppLevyUnitSpec {

  override def stubConfigPath = "./it/no-mappings"

  override def additionalConfiguration: Map[String, Any] = (super.additionalConfiguration - "microservice.private-mode") ++ Map(
    "microservice.private-mode" -> "false",
    "microservice.whitelisted-applications" -> "none")

  override implicit lazy final val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .in(Mode.Test)
    .build()

  override def beforeAll(cm: ConfigMap) {
  }

  override def afterAll(cm: ConfigMap) {
  }
}
