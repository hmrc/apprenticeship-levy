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

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, ITMPConnector}
import uk.gov.hmrc.apprenticeshiplevy.data.LevyDeclaration
import uk.gov.hmrc.play.microservice.controller.BaseController

trait SandboxLevyDeclarationController extends BaseController {

  def declarations(empref: String, months: Option[Int]) = Action.async { implicit request =>
    ETMPConnector.declarations(empref, months)
      .map(ds => Ok(Json.toJson(ds)))
  }

  def fractions(empref: String, months: Option[Int]) = Action.async { implicit request =>
    ITMPConnector.fractions(empref, months).map(fs => Ok(Json.toJson(fs)))
  }
}

object SandboxLevyDeclarationController extends SandboxLevyDeclarationController
