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

import play.api.hal.{HalLink, HalLinks}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.controllers.ApiController
import uk.gov.hmrc.apprenticeshiplevy.data.Links

import scala.concurrent.Future

trait SandboxEmprefRoutesController extends ApiController {

  val SandboxRegex = "^\\/?sandbox".r

  def routes(empref: String) = withValidAcceptHeader.async { implicit request =>
    Future {
      Links.data.get(empref).map { links =>
        val selfLink = HalLink("self", uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxEmprefRoutesController.routes(empref).url)
        val halLinks = Vector(selfLink) ++ links
        val transformedLinks = HalLinks(halLinks.map(stripSandboxForDev))
        Ok(Json.toJson(transformedLinks))
      }.getOrElse(NotFound)
    }
  }

  private def stripSandboxForDev(halLink: HalLink) =
    if (AppContext.env != "Dev") halLink.copy(href = SandboxRegex.replaceFirstIn(halLink.href, "")) else halLink
}

object SandboxEmprefRoutesController extends SandboxEmprefRoutesController
