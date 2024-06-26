/*
 * Copyright 2024 HM Revenue & Customs
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

package test.uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import test.uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig

trait WiremockService extends IntegrationTestConfig with StandardOutInformer {
  lazy val notifier = new WiremockTestInformerNotifier(info, verboseWiremockOutput)

  System.err.println(s"Configuring wire mock server to listen on ${stubHost}:${stubPort} using responses configured in ${stubConfigPath}")
  lazy val wireMockServer = new WireMockServer(wireMockConfig.notifier(notifier).usingFilesUnderDirectory(stubConfigPath).port(stubPort).bindAddress(stubHost))

  def start() = {
    wireMockServer.start()
  }

  def stop() = {
    wireMockServer.stop()
  }
}

object WiremockService extends WiremockService