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

import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDate
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, TaxYear}
import uk.gov.hmrc.apprenticeshiplevy.data.charges.Charge
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, LevyDeclarations, PayrollMonth}
import uk.gov.hmrc.play.http.NotFoundException

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>
  def etmpConnector: ETMPConnector

  def declarations(empref: String, months: Option[Int]) = withValidAcceptHeader.async { implicit request =>
    retrieveDeclarations(empref, months.getOrElse(48), LocalDate.now)
  }

  private[controllers] def retrieveDeclarations(empref: String, months: Int, now: LocalDate): Future[Result] = {
    val earliest = now.minusMonths(months)
    val taxYears = TaxYear.yearsInRange(earliest, now)

    Future
      .traverse(taxYears)(ty => declarationsForTaxYear(empref, ty))
      .map(ds => buildResult(ds.flatten, empref, earliest))
  }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String, earliest: LocalDate): Result = {
    case Seq() => ErrorNotFound.result
    case _ => Ok(Json.toJson(buildDeclarations(ds, empref, earliest)))
  }

  private[controllers] def buildDeclarations(ds: Seq[LevyDeclaration], empref: String, earliest: LocalDate): LevyDeclarations =
    LevyDeclarations(empref, None, ds.filter(_.submissionDate >= earliest))


  private[controllers] def declarationsForTaxYear(empref: String, taxYear: TaxYear): Future[Seq[LevyDeclaration]] = {
    etmpConnector.charges(empref.replace("/", ""), taxYear).map { charges =>
      charges.charges
        .filter(isLevyCharge)
        .flatMap(convertToDeclaration)
    }.recover {
      case t: NotFoundException => Seq()
    }
  }

  private[controllers] def isLevyCharge(charge: Charge): Boolean = charge.chargeType.contains("APPRENTICESHIP LEVY")

  private[controllers] def convertToDeclaration(charge: Charge): Seq[LevyDeclaration] = {
    charge.period.map { period =>
      val originalOrAmended = if (charge.mainType == "EYU") "amended" else "original"
      LevyDeclaration(PayrollMonth(2017, 3), period.value, originalOrAmended, period.endDate.getOrElse(new LocalDate(2017, 3, 3)))
    }
  }
}


