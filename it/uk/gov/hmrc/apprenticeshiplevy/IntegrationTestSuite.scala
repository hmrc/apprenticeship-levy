package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.Crypto
import java.util.UUID._
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import scala.util.Try

class IntegrationTestsSuite extends Suites(new ServiceLocatorRegistrationISpec,
                                           new DeclarationsEndpointISpec,
                                           new DefinitionEndpointISpec,
                                           new DocumentationEndpointISpec,
                                           new EmploymentCheckEndpointISpec,
                                           new EmploymentRefEndpointISpec,
                                           new FractionsEndpointISpec,
                                           new FractionsCalculationDateEndpointISpec,
                                           new RootEndpointISpec)
  with BeforeAndAfterAllConfigMap with IntegrationTestConfig {

  lazy val auuid1 = randomUUID()
  lazy val auuid2 = randomUUID()
  lazy val auuid3 = randomUUID()
  lazy val auuid4 = randomUUID()
  lazy val auuid5 = randomUUID()

  override def beforeAll(cm: ConfigMap) {
    sys.props.get("play.crypto.secret") match {
      case Some(_) => Console.println(s"[info] play.crypto.secret system property set.")
      case _ => Console.err.println(s"[${Console.YELLOW}warn${Console.RESET}] play.crypto.secret system property not set. ${Console.RED}Tests will fail.${Console.RESET}")
    }

    WiremockService.start()
    PlayService.start()


    Try(dFileToStr("./it/resources/data/input/mapping_url_1")).foreach { (validReadURL1) =>
      stubFor(get(urlEqualTo(validReadURL1)).withId(auuid1).atPriority(1).willReturn(aResponse().withStatus(200)))
      //println("validReadURL1: " + validReadURL1)
    }

    Try(dFileToStr("./it/resources/data/input/mapping_url_2")).foreach { (validReadURL2) =>
      stubFor(get(urlEqualTo(validReadURL2)).withId(auuid2).atPriority(1).willReturn(aResponse().withStatus(200)))
      //println("validReadURL2: " + validReadURL2)
    }

    Try(dFileToStr("./it/resources/data/input/mapping_url_3")).foreach { (faultURL1) =>
      stubFor(get(urlEqualTo(faultURL1)).withId(auuid4).atPriority(3).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
      //println("faultURL1: " + faultURL1)
    }

    Try(dFileToStr("./it/resources/data/input/mapping_url_4")).foreach { (invalidReadURL1) =>
      stubFor(get(urlMatching(invalidReadURL1)).withId(auuid5).atPriority(1).willReturn(aResponse().withStatus(200)))
      //println("invalidReadURL1: " + invalidReadURL1)
    }
  }

  override def afterAll(cm: ConfigMap) {
    PlayService.stop()
    WiremockService.stop()
  }
}

class NoWiremockIntegrationTestsSuite
  extends Suites(new PublicDefinitionEndpointISpec)
  with BeforeAndAfterAllConfigMap with IntegrationTestConfig {

  val playService = new PlayService() {
    override def stubConfigPath = "./it/no-mappings"
    override def additionalConfiguration: Map[String, Any] = (super.additionalConfiguration - "microservice.private-mode") ++ Map(
      "microservice.private-mode" -> "false",
      "microservice.whitelisted-applications" -> "none")
  }

  override def beforeAll(cm: ConfigMap) {
    playService.start()
  }

  override def afterAll(cm: ConfigMap) {
    playService.stop()
  }
}