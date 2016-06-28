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
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.connectors.{ETMPConnector, TaxYear}
import uk.gov.hmrc.apprenticeshiplevy.data.charges.Charge
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, LevyDeclarations, PayrollMonth}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>
  def etmpConnector: ETMPConnector


  def declarations(empref: String, months: Option[Int]) = withValidAcceptHeader.async { implicit request =>
    retrieveDeclarations(empref, months.getOrElse(48), LocalDate.now)
  }

  private[controllers] def retrieveDeclarations(empref: String, months: Int, now: LocalDate)(implicit hc: HeaderCarrier): Future[Result] = {
    val earliest = now.minusMonths(months)
    val taxYears = TaxYear.yearsInRange(earliest, now)

    Future
      .traverse(taxYears)(ty => declarationsForTaxYear(empref, ty))
      .map(ds => buildResult(ds.flatten.sortBy(_.submissionDate), empref, earliest))
  }

  private[controllers] def declarationsForTaxYear(empref: String, taxYear: TaxYear)(implicit hc: HeaderCarrier): Future[Seq[LevyDeclaration]] = {
    etmpConnector.charges(empref.replace("/", ""), taxYear).map { result =>
      result.charges
        .filter(isLevyCharge)
        .flatMap(convertToDeclaration(_, taxYear))
    }.recover {
      /*
      * The etmp charges call can return 404 if either the empref is unknown or there is no data for the tax year.
      * We don't know which one it might be, so convert a 404 to an empty result. The controller can decide
      * if it wants to return a 404 if all calls to `charges` return no results.
       */
      case t: NotFoundException => Seq()
    }
  }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String, earliest: LocalDate): Result =
    ds match {
      case Seq() => ErrorNotFound.result
      case _ => Ok(Json.toJson(buildDeclarations(ds, empref, earliest)))
    }

  private[controllers] def buildDeclarations(ds: Seq[LevyDeclaration], empref: String, earliest: LocalDate): LevyDeclarations =
    LevyDeclarations(empref, None, ds.filter(_.submissionDate >= earliest))

  private[controllers] def convertToDeclaration(charge: Charge, taxYear: TaxYear): Seq[LevyDeclaration] = {
    charge.period.map { period =>
      LevyDeclaration(
        PayrollMonth.forDate(period.startDate.getOrElse(taxYear.endDate)),
        period.value,
        originalOrAmended(charge),
        period.endDate.getOrElse(taxYear.endDate))
    }
  }

  // Likely to get more complex as we understand the Charges data better
  private[controllers] def isLevyCharge(charge: Charge): Boolean = charge.chargeType.contains("APPRENTICESHIP LEVY")

  // Likely to get more complex as we understand the Charges data better
  private[controllers] def originalOrAmended(charge: Charge) = if (charge.mainType == "EYU") "amended" else "original"

}


