package uk.gov.hmrc.apprenticeshiplevy.config

import org.scalatest._
import org.scalatest.Matchers._

import uk.gov.hmrc.apprenticeshiplevy._
import uk.gov.hmrc.apprenticeshiplevy.util._
import org.scalatestplus.play._

@DoNotDiscover
class ConfigurationISpec extends WiremockFunSpec
with ConfiguredServer with EitherValues with Matchers {
  describe("Application Configuration") {
    it ("should have an running app") {
      AppContext.maybeApp.isDefined shouldBe true
    }
    it ("should have an configuration") {
      AppContext.maybeConfiguration.isDefined shouldBe true
    }
    it ("should have an app url") {
      AppContext.appUrl shouldBe "http://microservice-name"
    }
    it ("should have an service locator url") {
      AppContext.serviceLocatorUrl shouldBe "http://localhost:8080"
    }
    it ("should have registration enabled") {
      AppContext.registrationEnabled shouldBe true
    }
    it ("should have private mode enabled") {
      AppContext.privateModeEnabled shouldBe true
    }
    it ("should have whitelist application id list") {
      AppContext.whitelistedApplicationIds shouldBe Array("myappid")
    }
    it ("should have DES environment") {
      AppContext.desEnvironment shouldBe "ist0"
    }
    it ("should have DES token") {
      AppContext.desToken shouldBe "ABC"
    }
    it ("should have metrics disabled") {
      AppContext.metricsEnabled shouldBe false
    }
    it ("should have an Auth url") {
      AppContext.authUrl shouldBe "http://localhost:8080"
    }
    it ("should have a DES url") {
      AppContext.desUrl shouldBe "http://localhost:8080"
    }
    it ("should have a Stub Auth url") {
      AppContext.stubAuthUrl shouldBe "http://localhost:8080"
    }
    it ("should have a Stub DES url") {
      AppContext.stubDesUrl shouldBe "http://localhost:8080"
    }
    it ("should have a Date regular expression") {
      AppContext.datePattern.isEmpty shouldBe false
    }
    it ("should have a Nino isEmpty expression") {
      AppContext.ninoPattern.isEmpty shouldBe false
    }
    it ("should have a Empref regular expression") {
      AppContext.employerReferencePattern.isEmpty shouldBe false
    }
    it ("should have default number of levy declaration years") {
      AppContext.defaultNumberOfDeclarationYears shouldBe 6
    }
    it ("should have metrics defined") {
      MicroserviceGlobal.microserviceMetricsConfig(app).isDefined shouldBe true
    }
    it ("should have audit filter disabled for controllers (only for integration tests)") {
      MicroserviceAuditFilter.controllerNeedsAuditing("uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController") shouldBe false
    }
    it ("should support NINO's with 'KC' prefix") {
      PathBinders.isValidNino("KC745625A", "ERRORCODE").right.value shouldBe "KC745625A"
    }
  }
}
