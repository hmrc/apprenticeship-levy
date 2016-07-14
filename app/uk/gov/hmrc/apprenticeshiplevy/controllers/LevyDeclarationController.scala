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
import play.api.mvc.Result
import uk.gov.hmrc.apprenticeshiplevy.connectors.{EmployerPaymentSummary, RTIConnector}
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotFound
import uk.gov.hmrc.apprenticeshiplevy.data.LevyDeclaration
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.Future

trait LevyDeclarationController {
  self: ApiController =>
  def rtiConnector: RTIConnector

  def declarations(empref: String, months: Option[Int]) = withValidAcceptHeader.async { implicit request =>
    retrieveDeclarations(empref, months)
      .map(ds => buildResult(ds.sortBy(_.submissionTime), empref))
  }

  private[controllers] def retrieveDeclarations(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[Seq[LevyDeclaration]] = {

    rtiConnector.eps(empref, months).map { lds =>
      lds.map(convertToDeclaration)
    }.recover {
      /*
      * The etmp charges call can return 404 if either the empref is unknown or there is no data for the tax year.
      * We don't know which one it might be, so convert a 404 to an empty result. The controller can decide
      * if it wants to return a 404 if all calls to `charges` return no results.
       */
      case t: NotFoundException => Seq()
    }
  }

  private[controllers] def buildResult(ds: Seq[LevyDeclaration], empref: String): Result = {
    if (ds.nonEmpty) Ok(Json.toJson(ds))
    else ErrorNotFound.result
  }

  private[controllers] def convertToDeclaration(employerPaymentSummary: EmployerPaymentSummary) =
    employerPaymentSummary match {
      case eps if eps.finalSubmission.exists(_.dateSchemeCeased.isDefined) =>
        LevyDeclaration(eps.eventId, eps.submissionTime,
          dateCeased = eps.finalSubmission.flatMap {
            fs => fs.dateSchemeCeased
          })
      case eps if eps.periodOfInactivity.isDefined =>
        LevyDeclaration(eps.eventId, eps.submissionTime,
          inactiveFrom = eps.periodOfInactivity.map(_.from),
          inactiveTo = eps.periodOfInactivity.map(_.to))
    }

}


