package uk.gov.hmrc.apprenticeshiplevy.config

import org.scalatest._
import org.scalatestplus.play._
import uk.gov.hmrc.apprenticeshiplevy._

@DoNotDiscover
class ConfigurationISpec extends WiremockFunSpec
with ConfiguredServer with EitherValues with Matchers {
  describe("Application Configuration") {
    it ("should support NINO's with 'KC' prefix") {
      PathBinders.isValidNino("KC745625A", "ERRORCODE").right.value shouldBe "KC745625A"
    }
  }
}
