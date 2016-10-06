package uk.gov.hmrc.apprenticeshiplevy

import uk.gov.hmrc.play.test.UnitSpec

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest._

import uk.gov.hmrc.apprenticeshiplevy.util.{WiremockService, IntegrationTestConfig}

trait WiremockConfig extends BeforeAndAfterEach with Informing {
  this: Suite =>

  lazy val uuid = java.util.UUID.randomUUID()

  override def beforeEach {
    WiremockService.notifier.testInformer = this.info
  }

  override def afterEach {
    WiremockService.notifier.testInformer = this.info
  }
}

trait WiremockSpec extends UnitSpec with GeneratorDrivenPropertyChecks with IntegrationTestConfig with WiremockConfig

trait WiremockFunSpec extends FunSpec with GeneratorDrivenPropertyChecks with WiremockConfig with IntegrationTestConfig