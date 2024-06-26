/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.uk.gov.hmrc.apprenticeshiplevy

import org.scalacheck.Gen
import org.scalatest._
import test.uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import org.scalatest.funspec.AnyFunSpec
import test.uk.gov.hmrc.apprenticeshiplevy.util.WiremockService

import java.util.UUID

trait WiremockConfig extends BeforeAndAfterEach with Informing {
  this: Suite =>

  lazy val uuid: UUID = java.util.UUID.randomUUID()

  override def beforeEach(): Unit = {
    WiremockService.notifier.testInformer = this.info
  }

  override def afterEach(): Unit = {
    WiremockService.notifier.testInformer = this.info
  }
}

trait WiremockFunSpec extends AnyFunSpec with WiremockConfig with IntegrationTestConfig {
    def standardDesHeaders(): Seq[(String,String)] = Seq("ACCEPT"->"application/vnd.hmrc.1.0+json",
                                                         "Environment"->"isit",
                                                         "Authorization"->"Bearer 2423324")
    def genEmpref: Gen[String] = (for {
      c <- Gen.alphaLowerChar
      cs <- Gen.listOf(Gen.alphaNumChar)
    } yield (c::cs).mkString).suchThat(_.forall(c => c.isLetter || c.isDigit))

    def genNino: Gen[String] = (for {
      c1 <- Gen.alphaUpperChar
      c2 <- Gen.alphaUpperChar
      cs <- Gen.listOf(Gen.numChar)
      c3 <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield s"$c1$c2${cs.mkString}$c3").suchThat(_.forall(c => c.isLetter || c.isDigit))
}