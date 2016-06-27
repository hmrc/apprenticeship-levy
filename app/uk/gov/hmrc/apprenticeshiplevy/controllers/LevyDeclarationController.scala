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

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.connectors.ETMPConnector
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, LevyDeclarations, PayrollMonth}

trait LevyDeclarationController {
  self: ApiController =>
  def etmpConnector: ETMPConnector

  def declarations(empref: String, months: Option[Int]) = withValidAcceptHeader.async { implicit request =>

    etmpConnector.charges(empref.replace("/", ""), "2017_18").map { charges =>
      val ds = charges.charges.filter(_.chargeType.contains("APPRENTICESHIP LEVY")).flatMap { charge =>
        charge.period.map { period =>
          val originalOrAmended = if (charge.mainType == "EYU") "amended" else "original"
          LevyDeclaration(PayrollMonth(2017, 3), period.value, originalOrAmended, period.endDate.getOrElse("2017-03-03"))
        }
      }

      Ok(Json.toJson(LevyDeclarations(empref, None, ds)))
    }
  }
}


