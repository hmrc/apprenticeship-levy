package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.Crypto
import java.util.UUID._
import com.github.tomakehurst.wiremock.client.WireMock._

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

  override def beforeAll(cm: ConfigMap) {
    WiremockService.start()
    PlayService.start()
    stubFor(get(urlEqualTo(Crypto.decryptAES("18e59cb4c2283077b719589a76e313da4529f6f4eff927757ffa01b6cc2ecebe21ae0a18adf30d74f635e5da559884be4d93722bd0f360140cc49f10863cd97e"))).withId(auuid1).atPriority(1).willReturn(aResponse().withStatus(200)))
    stubFor(get(urlEqualTo(Crypto.decryptAES("18e59cb4c2283077b719589a76e313da48d1e556726608f18b181c7e5b83d1367ba0a55480e83d5cd092418a35eda73973e52c35a74ab03bdc4b4178f375c203"))).withId(auuid2).atPriority(1).willReturn(aResponse().withStatus(200)))
    stubFor(get(urlEqualTo( Crypto.decryptAES("18e59cb4c2283077b719589a76e313da0879a0d4698db5456ec21a25ee047cd4c566006afbebd1a8f5ced40b47865577") )).withId(auuid3).atPriority(2).willReturn(aResponse().withStatus(401)))
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