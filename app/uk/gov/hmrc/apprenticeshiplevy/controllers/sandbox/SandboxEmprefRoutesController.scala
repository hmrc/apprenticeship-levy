/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox

import play.api.hal.{Hal, HalLinks}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.apprenticeshiplevy.controllers.ApiController
import uk.gov.hmrc.apprenticeshiplevy.data.Links

trait SandboxEmprefRoutesController extends ApiController {

  import SandboxLinkHelper._

  def routes(empref: String) = withValidAcceptHeader { implicit request =>
    Links.data.get(empref).map { links =>
      val self = selfLink(uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxEmprefRoutesController.routes(empref).url)
      Ok(Hal.hal(JsObject(Seq.empty), (self +: links).map(stripSandboxForDev).toVector))
    }.getOrElse(NotFound)
  }
}

object SandboxEmprefRoutesController extends SandboxEmprefRoutesController
