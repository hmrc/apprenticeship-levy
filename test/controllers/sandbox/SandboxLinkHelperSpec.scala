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

package controllers.sandbox

import play.api.hal.HalLink
import utils.SpecBase

class SandboxLinkHelperSpec extends SpecBase {
  val devHelper = new SandboxLinkHelper {
    override def env: String = "Dev"
  }

  val prodHelper = new SandboxLinkHelper {
    override def env: String = "Prod"
  }

  "stripSandboxForNonDev" must {
    "leave '/sandbox' on the front of a url if env is Dev" in {
      devHelper.stripSandboxForNonDev("/sandbox/test") mustBe "/sandbox/test"
    }

    "strip '/sandbox' from the front of a url if env is Prod" in {
      prodHelper.stripSandboxForNonDev("/sandbox/test") mustBe "/test"
    }

    "not strip sandbox from anywhere else in the url" in {
      prodHelper.stripSandboxForNonDev("/foo/sandbox") mustBe "/foo/sandbox"
    }

    "turn '/sandbox' into '/'" in {
      prodHelper.stripSandboxForNonDev("/sandbox") mustBe "/"
    }

    "turn '/sandbox/' into '/'" in {
      prodHelper.stripSandboxForNonDev("/sandbox/") mustBe "/"
    }

    "turn '/sandbox/foo/' into '/foo/'" in {
      prodHelper.stripSandboxForNonDev("/sandbox/foo/") mustBe "/foo/"
    }

    "return a HalLink with the converted and supplied arguments" in {
      val halLink = HalLink("rel", "link")
      prodHelper.stripSandboxForNonDev(halLink) mustBe HalLink("rel", "link")
    }
  }

}
