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

package uk.gov.hmrc.apprenticeshiplevy.data.des


import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.des.DesignatoryDetailsData._


class DesignatoryDetailsSpec extends AppLevyUnitSpec {

  "Designatory Details" should {
    "Parse HodName json correctly" in {
      val json = Json.parse("""{"nameLine1":"nameLine1","nameLine2":"nameLine2"}""")
      json.validate[HodName].isSuccess shouldBe true
      json.as[HodName] shouldBe HodName(Some("nameLine1"), Some("nameLine2"))
    }

    "Parse HodAddress json correctly" in {
      val json = Json.parse("""{"addressLine1":"hodAddressLine1","addressLine2":"hodAddressLine2"}""")
      json.validate[HodAddress].isSuccess shouldBe true
      json.as[HodAddress] shouldBe HodAddress(Some("hodAddressLine1"), Some("hodAddressLine2"))
    }

    "Parse HodContact json correctly" in {
      val json = Json.parse("""{"telephone":{"telephoneNumber":"hodTelephone"},"email":{"primary":"hodEmail"}}""")
      json.validate[HodContact].isSuccess shouldBe true
      json.as[HodContact] shouldBe HodContact(Some(HodTelephone(Some("hodTelephone"))), Some(HodEmail(Some("hodEmail"))))
    }
  }
}
