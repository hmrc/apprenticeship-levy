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

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.Json
import uk.gov.hmrc.play.controllers.RestFormats

case class EmpRefs(officeNumber: String, payeRef: String, aoRef: String)

object EmpRefs {
  implicit val formats = Json.format[EmpRefs]
}

case class DateRange(from: LocalDate, to: LocalDate)

object DateRange {
  implicit val formats = Json.format[DateRange]
}

case class RecoverableAmountsYTD(taxMonth: Option[Int] = None,
                                 smpRecovered: Option[BigDecimal] = None,
                                 sppRecovered: Option[BigDecimal] = None,
                                 sapRecovered: Option[BigDecimal] = None,
                                 shppRecovered: Option[BigDecimal] = None,
                                 nicCompensationOnSMP: Option[BigDecimal] = None,
                                 nicCompensationOnSPP: Option[BigDecimal] = None,
                                 nicCompensationOnSAP: Option[BigDecimal] = None,
                                 nicCompensationOnShPP: Option[BigDecimal] = None,
                                 cisDeductionsSuffered: Option[BigDecimal] = None)

case class ApprenticeshipLevy(levyDueYTD: BigDecimal, taxMonth: Int, annualAllce: BigDecimal)

object ApprenticeshipLevy {
  implicit val formats = Json.format[ApprenticeshipLevy]
}

object RecoverableAmountsYTD {
  implicit val formats = Json.format[RecoverableAmountsYTD]
}

case class Account(accountHoldersName: String,
                   accountNo: String,
                   sortCode: String,
                   buildingSocRef: Option[String] = None)

object Account {
  implicit val formats = Json.format[Account]
}

case class FinalSubmission(becauseSchemeCeased: Yes = None, dateSchemeCeased: Option[LocalDate] = None, forYear: Yes = None)

object FinalSubmission {
  implicit val formats = Json.format[FinalSubmission]
}

case class EmployerPaymentSummary(eventId: Long,
                                  submissionTime: DateTime,
                                  empRefs: EmpRefs,
                                  noPaymentForPeriod: Yes = None,
                                  noPaymentDates: Option[DateRange] = None,
                                  periodOfInactivity: Option[DateRange] = None,
                                  empAllceInd: YesNo = None,
                                  recoverableAmountsYTD: Option[RecoverableAmountsYTD] = None,
                                  apprenticeshipLevy: Option[ApprenticeshipLevy] = None,
                                  account: Option[Account] = None,
                                  relatedTaxYear: String,
                                  finalSubmission: Option[FinalSubmission] = None)

object EmployerPaymentSummary {
  implicit val dateTimeFormats = RestFormats.dateTimeFormats
  implicit val formats = Json.format[EmployerPaymentSummary]
}
