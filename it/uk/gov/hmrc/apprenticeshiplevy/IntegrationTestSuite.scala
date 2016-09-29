package uk.gov.hmrc.apprenticeshiplevy

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.util._
import uk.gov.hmrc.play.test.UnitSpec

class IntegrationTestsSuite extends Suites(new uk.gov.hmrc.apprenticeshiplevy.sandbox.Suite,
                                           new DocumentationControllerISpec,
                                           new ServiceLocatorRegistrationISpec)
  with BeforeAndAfterAllConfigMap with IntegrationTestConfig {

  override def beforeAll(cm: ConfigMap) {
    WiremockService.start()
    PlayService.start()
  }

  override def afterAll(cm: ConfigMap) {
    PlayService.stop()
    WiremockService.stop()
  }
}