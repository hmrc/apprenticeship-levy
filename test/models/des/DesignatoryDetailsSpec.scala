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

package models.des


import play.api.libs.json.Json
import utils.SpecBase
import models.des.DesignatoryDetailsData.*


class DesignatoryDetailsSpec extends SpecBase {

  "Designatory Details" must {
    "Parse HodName json correctly" in {
      val json = Json.parse("""{"nameLine1":"nameLine1","nameLine2":"nameLine2"}""")
      json.validate[HodName].isSuccess mustBe true
      json.as[HodName] mustBe HodName(Some("nameLine1"), Some("nameLine2"))
    }

    "Parse HodAddress json correctly" in {
      val json = Json.parse("""{"addressLine1":"hodAddressLine1","addressLine2":"hodAddressLine2"}""")
      json.validate[HodAddress].isSuccess mustBe true
      json.as[HodAddress] mustBe HodAddress(Some("hodAddressLine1"), Some("hodAddressLine2"))
    }

    "Parse HodContact json correctly" in {
      val json = Json.parse("""{"telephone":{"telephoneNumber":"hodTelephone"},"email":{"primary":"hodEmail"}}""")
      json.validate[HodContact].isSuccess mustBe true
      json.as[HodContact] mustBe HodContact(Some(HodTelephone(Some("hodTelephone"))), Some(HodEmail(Some("hodEmail"))))
    }
  }
}
