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

package uk.gov.hmrc.apprenticeshiplevy.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class DecodePathSpec extends AnyWordSpec with Matchers {

  "DecodePath" should {
    "decode a double encoded path" in {

      val doubleEncodedPath = "/epaye/123%252FAB12345"
      val expectedPath = "/epaye/123%2FAB12345"
      val result = DecodePath.decodeAnyDoubleEncoding(doubleEncodedPath)

      result shouldBe expectedPath
    }

    "not change a single encoded path" in {
      val singleEncodedPath = "/epaye/123%2FAB12345"
      val expectedPath = "/epaye/123%2FAB12345"
      val result = DecodePath.decodeAnyDoubleEncoding(singleEncodedPath)

      result shouldBe expectedPath
    }

    "manage to decode a url with braces" in {
      val singleEncodedPath = "/epaye/123%252FAB12345/employed/{nino}"
      val expectedPath = "/epaye/123%2FAB12345/employed/{nino}"
      val result = DecodePath.decodeAnyDoubleEncoding(singleEncodedPath)

      result shouldBe expectedPath
    }
  }

}
