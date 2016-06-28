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

import org.joda.time.LocalDate
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, TaxYear}
import uk.gov.hmrc.apprenticeshiplevy.data.charges.Charge
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, LevyDeclarations, PayrollMonth}
import uk.gov.hmrc.play.http.NotFoundException

import com.github.nscala_time.time.Imports._

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>
  def etmpConnector: ETMPConnector

  def declarations(empref: String, months: Option[Int]) = withValidAcceptHeader.async { implicit request =>
    val now = LocalDate.now
    val earliest = now.minusMonths(months.getOrElse(48))

    val fs: Seq[Future[Seq[LevyDeclaration]]] =
      TaxYear.yearsInRange(earliest, now).map { taxYear =>
        etmpConnector.charges(empref.replace("/", ""), taxYear).map { charges =>
          charges.charges
            .filter(_.chargeType.contains("APPRENTICESHIP LEVY"))
            .flatMap(convertCharge)
            .filter(_.submissionDate >= earliest)
        }.recover {
          case t: NotFoundException => Seq()
        }
      }

    Future.reduce(fs)(_ ++ _).map {
      case Seq() => ErrorNotFound.result
      case ds => Ok(Json.toJson(LevyDeclarations(empref, None, ds)))
    }
  }

  def convertCharge(charge: Charge): Seq[LevyDeclaration] = {
    charge.period.map { period =>
      val originalOrAmended = if (charge.mainType == "EYU") "amended" else "original"
      LevyDeclaration(PayrollMonth(2017, 3), period.value, originalOrAmended, period.endDate.getOrElse(new LocalDate(2017, 3, 3)))
    }
  }
}


