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

package uk.gov.hmrc.apprenticeshiplevy.data.audit

import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}

class ALAEventSpec extends AppLevyUnitSpec {

  "toDataEvent" should {
    "return a dataEvent" when {
      "a http status is supplied" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq("Content-Type" -> "application/json"))

        val event = ALAEvent("event1")
        event.toDataEvent(200).detail shouldBe Map("upstream_http_status" -> "200")

      }
      "a http status and exception is supplied" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq("Content-Type" -> "application/json"))
        val exception = new BadRequestException("bad request")

        val event = ALAEvent("event1")
        event.toDataEvent(404, exception).detail shouldBe Map("upstream_http_status" -> "404", "exception" -> "uk.gov.hmrc.http.BadRequestException: bad request")
      }
    }
  }

}
