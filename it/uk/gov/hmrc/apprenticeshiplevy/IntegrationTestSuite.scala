package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec
import play.api.libs.Crypto
import java.util.UUID._
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault

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

  override def beforeAll(cm: ConfigMap) {
    WiremockService.start()
    PlayService.start()
    val validReadURL1enc = "18e59cb4c2283077b719589a76e313da4529f6f4eff927757ffa01b6cc2ecebe21ae0a18adf30d74f635e5da559884be4d93722bd0f360140cc49f10863cd97e"
    val validReadURL1 = Crypto.decryptAES(validReadURL1enc)
    stubFor(get(urlEqualTo(validReadURL1)).withId(auuid1).atPriority(1).willReturn(aResponse().withStatus(200)))
    println("validReadURL1: " + validReadURL1)

    val validReadURL2enc = "18e59cb4c2283077b719589a76e313da48d1e556726608f18b181c7e5b83d1367ba0a55480e83d5cd092418a35eda73973e52c35a74ab03bdc4b4178f375c203"
    val validReadURL2 = Crypto.decryptAES(validReadURL2enc)
    stubFor(get(urlEqualTo(validReadURL2)).withId(auuid2).atPriority(1).willReturn(aResponse().withStatus(200)))
    println("validReadURL2: " + validReadURL2)

    val invalidReadURL1enc = "18e59cb4c2283077b719589a76e313da0879a0d4698db5456ec21a25ee047cd4c566006afbebd1a8f5ced40b47865577"
    val invalidReadURL1 = Crypto.decryptAES(invalidReadURL1enc)
    stubFor(get(urlEqualTo(invalidReadURL1)).withId(auuid3).atPriority(1).willReturn(aResponse().withStatus(404)))
    println("invalidReadURL1: " + invalidReadURL1)

    val closeConnectionReadURL1enc = "18e59cb4c2283077b719589a76e313da0a2b7fa681e8f70f76a0e1418104c792e625da469b6c15bb2839797d8c091a16a40d1904428c19b30469e36eab4725ab"
    val closeConnectionReadURL1 = Crypto.decryptAES(closeConnectionReadURL1enc)
    stubFor(get(urlEqualTo(closeConnectionReadURL1)).withId(auuid4).atPriority(3).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
    println("closeConnectionReadURL1: " + closeConnectionReadURL1)
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