package uk.gov.hmrc.apprenticeshiplevy

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.apprenticeshiplevy.controllers.DocumentationController
import uk.gov.hmrc.apprenticeshiplevy.util.{MicroserviceLocalRunSupport, WiremockServiceLocatorSupport}
import uk.gov.hmrc.play.test.UnitSpec

class ServiceLocatorRegistrationISpec
  extends UnitSpec with MockitoSugar with ScalaFutures with WiremockServiceLocatorSupport with BeforeAndAfter
  with IntegrationPatience {

  before {
    startMockServer()
    stubRegisterEndpoint(204)
  }

  after {
    stopMockServer()
  }

  trait Setup {
    val documentationController = new DocumentationController() {
      override def documentation(version: String, endpoint: String) =
      // resources in it are not under /public
        super.at(s"/documentation/$version", s"${endpoint.replaceAll(" ", "-")}.xml")
    }
    val request = FakeRequest()
  }

  "microservice" should {

    "register itelf to service-locator" in new MicroserviceLocalRunSupport with Setup {
      override val additionalConfiguration = Map(
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name.service",
        "microservice.services.service-locator.host" -> stubHost,
        "microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.enabled" -> "true")
      run { () => {
          verify(1, postRequestedFor(urlMatching("/registration")).
            withHeader("content-type", equalTo("application/json")).
            withRequestBody(equalTo(regPayloadStringFor("application-name", "http://microservice-name.service"))))
        }
      }
    }

    "provide definition endpoint and documentation endpoints for each api" in new MicroserviceLocalRunSupport with Setup {
      override val additionalConfiguration = Map(
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name.service",
        "microservice.services.service-locator.host" -> stubHost,
        "microservice.services.service-locator.port" -> stubPort,
        "microservice.services.service-locator.enabled" -> "true")

      run { () => {
        def verifyDocumentationPresent(version: String, endpointName: String) {
          withClue(s"Getting documentation version '$version' of endpoint '$endpointName'") {
            val documentationResult = documentationController.documentation(version, endpointName)(request)
            status(documentationResult) shouldBe 200
          }
        }

        val result = documentationController.at("/api", "definition.json")(request)
        status(result) shouldBe 200

        val jsonResponse = jsonBodyOf(result).futureValue

        val versions = (jsonResponse \\ "version") map (_.as[String])
        val endpointNames = (jsonResponse \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String].toLowerCase))

        versions.zip(endpointNames).flatMap { case (version, endpoint) =>
          endpoint.map(endpointName => (version, endpointName))
        }.foreach { case (version, endpointName) => verifyDocumentationPresent(version, endpointName) }
      }
      }
    }
  }
}