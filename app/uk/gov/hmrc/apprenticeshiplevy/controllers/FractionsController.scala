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
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.connectors.DesConnector
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.DateRange
import uk.gov.hmrc.play.http._
import scala.concurrent.Future

trait FractionsController {
  self: DesController =>
  def desConnector: DesConnector

  // scalastyle:off
  def fractions(ref: EmploymentReference, fromDate: Option[LocalDate], toDate: Option[LocalDate]) = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    if (fromDate.isDefined && toDate.isDefined && fromDate.get.isAfter(toDate.get)) {
      Future.successful(ErrorResponses.ErrorFromDateAfterToDate.result)
    } else {
      desConnector.fractions(toDESFormat(ref.empref), DateRange(fromDate, toDate)) map { fs =>
        Ok(Json.toJson(fs))
      } recover desErrorHandler
    }
  }
}

/**
  * This needs to be a separate trait as the fractions endpoint needs to have auth enabled, but
  * the calculation date endpoint doesn't
  */
trait FractionsCalculationDateController {
  self: DesController =>
  def desConnector: DesConnector

  // scalastyle:off
  def fractionCalculationDate = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    desConnector.fractionCalculationDate map { date =>
      Ok(Json.toJson(date))
    } recover desErrorHandler
  }
}
