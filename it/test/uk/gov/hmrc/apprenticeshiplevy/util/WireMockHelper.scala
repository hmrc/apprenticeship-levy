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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{get, post, urlEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import uk.gov.hmrc.apprenticeshiplevy.util.StubbingData.{auuid6, stubbedConfigPath}
import uk.gov.hmrc.http.test.PortFinder

import java.util.UUID

trait WireMockHelper extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  protected val server: WireMockServer = new WireMockServer(wireMockConfig.usingFilesUnderDirectory(stubbedConfigPath).port(PortFinder.findFreePort(portRange = 6001 to 7000)))

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  def stubGetServer(willReturn: ResponseDefinitionBuilder, url: String): StubMapping =
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(
          willReturn
        )
    )

  def stubPostServer(willReturn: ResponseDefinitionBuilder, url: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(url))
        .willReturn(
          willReturn
        )
    )

  def stubGetServerWithId(willReturn: ResponseDefinitionBuilder, url: String, uuid: UUID = auuid6): StubMapping =
    server.stubFor(
      get(urlEqualTo(url))
        .withId(uuid)
        .willReturn(
          willReturn
        )
    )

  def stubPostServerWithId(willReturn: ResponseDefinitionBuilder, url: String, uuid: UUID = auuid6): StubMapping =
    server.stubFor(
      post(urlEqualTo(url))
        .withId(uuid)
        .willReturn(
          willReturn
        )
    )
}
