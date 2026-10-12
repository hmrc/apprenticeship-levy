/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import config.AppContext
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import utils.AppLevyUnitSpec

import java.util.UUID.randomUUID

class IntegrationPlatformConnectorSpec extends AppLevyUnitSpec {

  private val connector = new SandboxIntegrationPlatformConnector(mock[HttpClientV2], mock[AppContext])

  "correlationId" must {
    "be returned from header carrier" in {
      val correlationId                  = randomUUID.toString
      val hcWithRequestId: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId(correlationId)))

      val result = connector.correlationId(using hcWithRequestId)

      result must startWith(correlationId.substring(0, 23))
    }

    "be generated when not in header carrier" in {
      val hcWithoutRequestId: HeaderCarrier = HeaderCarrier(requestId = None)

      val result = connector.correlationId(using hcWithoutRequestId)

      result must fullyMatch regex """[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"""
    }
  }
}
