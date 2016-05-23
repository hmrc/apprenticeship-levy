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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.apprenticeshiplevy.connectors.ETMPConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object LevyDeclarationController extends LevyDeclarationController

trait LevyDeclarationController extends BaseController {
  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

  def declarations(empref: String, months: Option[Int]) = Action.async { implicit request =>
    ETMPConnector.declarations(empref, months).map {decls =>
      Ok(Json.toJson(decls))
    }
  }
}
