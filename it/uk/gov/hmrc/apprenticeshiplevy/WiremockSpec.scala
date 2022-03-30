package uk.gov.hmrc.apprenticeshiplevy

import org.scalacheck.Gen
import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import uk.gov.hmrc.apprenticeshiplevy.util.WiremockService
import org.scalatest.funspec.AnyFunSpec

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

trait WiremockFunSpec extends AnyFunSpec with WiremockConfig with IntegrationTestConfig {
    def standardDesHeaders(): Seq[(String,String)] = Seq(("ACCEPT"->"application/vnd.hmrc.1.0+json"),
                                                         ("Environment"->"isit"),
                                                         ("Authorization"->"Bearer 2423324"))
    def genEmpref: Gen[String] = (for {
      c <- Gen.alphaLowerChar
      cs <- Gen.listOf(Gen.alphaNumChar)
    } yield (c::cs).mkString).suchThat(_.forall(c => c.isLetter || c.isDigit))

    def genNino: Gen[String] = (for {
      c1 <- Gen.alphaUpperChar
      c2 <- Gen.alphaUpperChar
      cs <- Gen.listOf(Gen.numChar)
      c3 <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield (c1+c2+cs.mkString+c3)).suchThat(_.forall(c => c.isLetter || c.isDigit))
}