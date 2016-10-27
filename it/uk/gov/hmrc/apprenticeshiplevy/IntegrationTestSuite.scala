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
  lazy val auuid5 = randomUUID()

  override def beforeAll(cm: ConfigMap) {
    sys.props.get("play.crypto.secret") match {
      case Some(secret) => {
        val key = secret.substring(0, 16)
        WiremockService.start()
        PlayService.start()
        val validReadURL1enc = "48297d60b292476c11c8ebd8269165f2380be895579b329a75517e385272517ae54e2439cb6d1e64445c6da0248684f1c7933b0e6991164c4547ac3032737be4"
        val validReadURL1 = Crypto.decryptAES(validReadURL1enc, key)
        stubFor(get(urlEqualTo(validReadURL1)).withId(auuid1).atPriority(1).willReturn(aResponse().withStatus(200)))
        println("validReadURL1: " + validReadURL1)
        //println(s"'${Crypto.encryptAES(validReadURL1, key)}'")

        val validReadURL2enc = "48297d60b292476c11c8ebd8269165f25b7821c8dcc9fca9546ba679df6106c7892f6fa3f35038ed2fc3778ee60d7186f3b1304045378ca2d09145631f202d5c"
        val validReadURL2 = Crypto.decryptAES(validReadURL2enc, key)
        stubFor(get(urlEqualTo(validReadURL2)).withId(auuid2).atPriority(1).willReturn(aResponse().withStatus(200)))
        println("validReadURL2: " + validReadURL2)
        //println(s"'${Crypto.encryptAES(validReadURL2, key)}'")

        val faultURL1enc = "48297d60b292476c11c8ebd8269165f242811bb4a3c861d8e01f8a30bb11621d986007148914440551fbb445ce5eebbe904bac64cfbf220d82cefa2cbc71508e"
        val faultURL1 = Crypto.decryptAES(faultURL1enc, key)
        stubFor(get(urlEqualTo(faultURL1)).withId(auuid4).atPriority(3).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)))
        println("faultURL1: " + faultURL1)
        //println(s"'${Crypto.encryptAES(faultURL1, key)}'")

        val invalidReadURL1enc = "48297d60b292476c11c8ebd8269165f2c00763f212a09bd5407513e830140d6f40a87f692300fbe2bea033e05bf266b18d38c0ce8c7d84354511073858c8a52b47c57717a271fd5a6ba8c2d3e6f0c53959b7ffe0a2b63fcd8a7360558a7eae09"
        val invalidReadURL1 = Crypto.decryptAES(invalidReadURL1enc, key)
        stubFor(get(urlMatching(invalidReadURL1)).withId(auuid5).atPriority(1).willReturn(aResponse().withStatus(200)))
        println("invalidReadURL1: " + invalidReadURL1)
        //println(s"'${Crypto.encryptAES(invalidReadURL1, key)}'")
      }
      case _ => {
        Console.err.println(s"${Console.RED} >>>>> play.crypto.secret system property not set ${Console.RESET}")
        fail("play.crypto.secret system property not set")
      }
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