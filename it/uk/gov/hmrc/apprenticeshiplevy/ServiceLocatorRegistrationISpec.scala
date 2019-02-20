package uk.gov.hmrc.apprenticeshiplevy

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.DoNotDiscover
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play._
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import uk.gov.hmrc.apprenticeshiplevy.data.api.ServiceLocatorRegistration
import uk.gov.hmrc.apprenticeshiplevy.util.WiremockService
import uk.gov.hmrc.play.test.UnitSpec

@DoNotDiscover
class ServiceLocatorRegistrationISpec
  extends UnitSpec with ScalaFutures
  with IntegrationPatience with IntegrationTestConfig with ConfiguredServer {

  trait Setup extends IntegrationTestConfig {
    def regPayloadStringFor(serviceName: String, serviceUrl: String): String =
      Json.toJson(ServiceLocatorRegistration(serviceName, serviceUrl, Some(Map("third-party-api" -> "true")))).toString

    val wiremockServer = WiremockService.wireMockServer
  }

  "API" should {
    "register itelf to service-locator" in new Setup {
      pending
      // Wiremock and Play started by IntegrationTestSuite so this test is simply to verify service registered on start up.
      wiremockServer.verify(1, postRequestedFor(urlMatching("/registration")).
          withHeader("content-type", equalTo("application/json")).
          withRequestBody(equalTo(regPayloadStringFor("application-name", additionalConfiguration.getOrElse("appUrl", "https://microservice-name").toString))))
    }
  }
}

@DoNotDiscover
class ServiceLocatorRegistrationISpec2
  extends UnitSpec with ScalaFutures
  with IntegrationPatience with IntegrationTestConfig with ConfiguredServer {

  trait Setup extends IntegrationTestConfig {
    def regPayloadStringFor(serviceName: String, serviceUrl: String): String =
      Json.toJson(ServiceLocatorRegistration(serviceName, serviceUrl, Some(Map("third-party-api" -> "true")))).toString

    val wiremockServer = WiremockService.wireMockServer
  }

  "API" should {
    "not register itelf to service-locator" in new Setup {
      // Wiremock and Play started by IntegrationTestSuite so this test is simply to verify service registered on start up.
      wiremockServer.verify(0, postRequestedFor(urlMatching("/registration")).
          withHeader("content-type", equalTo("application/json")).
          withRequestBody(equalTo(regPayloadStringFor("application-name", additionalConfiguration.getOrElse("appUrl", "http://microservice-name").toString))))
    }
  }
}
