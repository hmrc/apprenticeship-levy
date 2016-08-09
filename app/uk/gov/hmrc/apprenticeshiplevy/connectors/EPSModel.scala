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

package uk.gov.hmrc.apprenticeshiplevy.connectors

import org.joda.time.{LocalDate, LocalDateTime}
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.play.controllers.RestFormats


case class ApprenticeshipLevy(levyDueYTD: BigDecimal, taxMonth: Int, annualAllce: BigDecimal)

object ApprenticeshipLevy {
  implicit val formats = Json.format[ApprenticeshipLevy]
}

case class FinalSubmission(becauseSchemeCeased: Yes = None, dateSchemeCeased: Option[LocalDate] = None, forYear: Yes = None)

object FinalSubmission {
  implicit val formats = Json.format[FinalSubmission]
}

case class EmployerPaymentSummary(eventId: Long,
                                  submissionTime: LocalDateTime,
                                  noPaymentForPeriod: Yes = None,
                                  noPaymentDates: Option[ClosedDateRange] = None,
                                  periodOfInactivity: Option[ClosedDateRange] = None,
                                  apprenticeshipLevy: Option[ApprenticeshipLevy] = None,
                                  relatedTaxYear: String,
                                  finalSubmission: Option[FinalSubmission] = None)

object EmployerPaymentSummary {
  implicit val drFormats = Json.format[ClosedDateRange]
  implicit val ldtFormats = RestFormats.localDateTimeFormats
  implicit val formats = Json.format[EmployerPaymentSummary]
}
