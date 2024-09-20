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

package test.uk.gov.hmrc.apprenticeshiplevy.config

import scala.io.{BufferedSource, Source}
import scala.util.Using

trait IntegrationTestConfig {
  System.setProperty("logger.resource","logback-test.xml")

  def fileToStr(filename: String): String = {
    val fileBuffer: BufferedSource = Source.fromFile(s"$filename")

    Using(fileBuffer) {
      file => file.getLines().mkString("\n")
    }.get
  }

  def aesKey: String = sys.props.get("play.http.secret.key").map(_.substring(0, 16)).getOrElse("")
  def verboseWiremockOutput: Boolean = sys.props.getOrElse("WIREMOCK_VERBOSE_OUTPUT", "false").toBoolean
  def stubPort: Int = sys.props.getOrElse("WIREMOCK_PORT", "8080").toInt
  def stubHost: String = sys.props.getOrElse("WIREMOCK_HOST", "localhost")
  def stubConfigPath: String = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/test/resources")
  def resourcePath: String = sys.props.getOrElse("RESOURCE_PATH", "./it/test/resources")

  // val wireMockUrl = s"http://$stubHost:$stubPort"
  // apprenticeship-levy
  def testHost: String = sys.props.getOrElse("MICROSERVICE_HOST", "localhost")
  def testPort: Int = sys.props.getOrElse("MICROSERVICE_PORT", "9001").toInt

  def localMicroserviceUrl: String = s"http://$testHost:$testPort"
  def microserviceUrl: String = sys.props.getOrElse("MICROSERVICE_URL", s"http://localhost:$testPort")

  val controllerSettings: Seq[(String, String)] = Seq("LiveLevyDeclarationController",
                               "LiveRootController",
                               "LiveEmprefController",
                               "LiveFractionsController",
                               "LiveFractionsCalculationDateController",
                               "LiveEmploymentCheckController")
    .map(a=>(s"controllers.uk.gov.hmrc.apprenticeshiplevy.controllers.live.$a.needsAuditing","false"))

  def additionalConfiguration: Map[String, Any] = Map[String, Any](
        "play.ws.timeout.request" -> "500 milliseconds",
        "play.ws.timeout.connection" -> "500 milliseconds",
        "http.port" -> testPort,
        "auditing.enabled" -> "false",
        "microservice.private-mode" -> "true",
        "appName" -> "application-name",
        "appUrl" -> "http://microservice-name",
        "microservice.metrics.graphite.enabled" -> "false",
        "microservice.services.stub-auth.host" -> stubHost,
        "microservice.services.stub-auth.port" -> stubPort,
        "microservice.services.stub-auth.path" -> "",
        "microservice.services.stub-des.host" -> stubHost,
        "microservice.services.stub-des.port" -> stubPort,
        "microservice.services.stub-des.path" -> "",
        "microservice.services.des.host" -> stubHost,
        "microservice.services.des.port" -> stubPort,
        "microservice.services.des.path" -> "",
        "microservice.services.auth.host" -> stubHost,
        "microservice.services.auth.port" -> stubPort,
        "microservice.services.auth.path" -> "",
        "microservice.allowlisted-applications" -> "myappid1,myappid2"
        ) ++ Map(controllerSettings: _*)
}
