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

import com.github.nscala_time.time.Imports._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, ETMPLevyDeclaration, ITMPConnector}
import uk.gov.hmrc.apprenticeshiplevy.data.{EnglishFraction, LevyDeclaration, LevyDeclarations}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

object SandboxLevyDeclarationController extends SandboxLevyDeclarationController

trait SandboxLevyDeclarationController extends BaseController {

  def declarations(empref: String, months: Option[Int]) = Action.async { implicit request =>

    // Kick these off concurrently
    val edeclF = ETMPConnector.declarations(empref, months)
    val fractionsF: Future[List[EnglishFraction]] = ITMPConnector.fractions(empref, months)

    val decls = for {
      edecls <- edeclF
      fractions <- fractionsF
    } yield LevyDeclarations(empref, edecls.map(mergeFraction(_, fractions)))

    decls.map(ds => Ok(Json.toJson(ds)))
  }

  def mergeFraction(decl: ETMPLevyDeclaration, fractions: List[EnglishFraction]): LevyDeclaration = {
    val fraction = fractions.sortBy(_.calculatedAt).reverse.find(_.calculatedAt <= decl.payrollMonth.startDate)

    LevyDeclaration(decl.payrollMonth, decl.amount, decl.submissionType, decl.submissionDate, fraction)
  }
}
