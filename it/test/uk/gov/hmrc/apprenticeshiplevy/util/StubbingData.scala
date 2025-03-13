/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.util

import org.scalacheck.Gen

import java.util.UUID
import java.util.UUID.randomUUID
import scala.io.{BufferedSource, Source}
import scala.util.Using

object StubbingData {

  def stubbedConfigPath: String = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/test/resources")
  def resourcePath: String = sys.props.getOrElse("RESOURCE_PATH", "./it/test/resources")
  def standardDesHeaders(): Seq[(String,String)] = Seq("ACCEPT"->"application/vnd.hmrc.1.0+json",
    "Environment"->"isit",
    "Authorization"->"Bearer 2423324")
  def asString(filename: String): String = {
    val fileBuffer: BufferedSource = Source.fromFile(s"$resourcePath/data/expected/$filename")

    Using(fileBuffer) {
      file => file.getLines().mkString("\n")
    }.get
  }

  lazy val auuid1: UUID = randomUUID()
  lazy val auuid2: UUID = randomUUID()
  lazy val auuid3: UUID = randomUUID()
  lazy val auuid4: UUID = randomUUID()
  lazy val auuid5: UUID = randomUUID()
  lazy val auuid6: UUID = randomUUID()

  val validReadURL1 = "/authorise/read/epaye/AB12345?privilegedAccess=read:apprenticeship-levy"
  val validReadURL2 = "/authorise/read/epaye/123%2FAB12345?privilegedAccess=read:apprenticeship-levy"
  val faultURL1 = "/authorise/read/epaye/malformed?privilegedAccess=read:apprenticeship-levy"
  val invalidReadURL1 = "/authorise/read/epaye/(400|401|403|404|500|503|empty|malformed|timeout)%2FAB12345\\?privilegedAccess=read:apprenticeship-levy"
  val validRead = "/authorise/read/epaye/(.*)\\?privilegedAccess=read:apprenticeship-levy"

  def wireMockConfiguration(serverPort: Int): Map[String, Any] = Map(
    "play.ws.timeout.request" -> "500 milliseconds",
    "play.ws.timeout.connection" -> "500 milliseconds",
    "microservice.metrics.graphite.enabled" -> "false",
    "microservice.services.stub-auth.port" -> serverPort,
    "microservice.services.stub-auth.path" -> "",
    "microservice.services.stub-des.port" -> serverPort,
    "microservice.services.stub-des.path" -> "",
    "microservice.services.des.port" -> serverPort,
    "microservice.services.des.path" -> "",
    "microservice.services.auth.port" -> serverPort,
    "microservice.services.auth.path" -> "",
    "microservice.private-mode" -> "true",
    "microservice.allowlisted-applications" -> "myappid1,myappid2"
  )

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
