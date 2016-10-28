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

import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}
import play.api.libs.json._
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange


case class ApprenticeshipLevy(levyDueYTD: BigDecimal, taxMonth: Int, annualAllce: BigDecimal)

object ApprenticeshipLevy {
  implicit val formats = Json.format[ApprenticeshipLevy]
}

case class FinalSubmission(becauseSchemeCeased: Option[String] = None, dateSchemeCeased: Option[LocalDate] = None, forYear: Option[String] = None)

object FinalSubmission {
  implicit val formats = Json.format[FinalSubmission]
}

case class EmployerPaymentSummary(eventId: Long,
                                  submissionTime: LocalDateTime,
                                  noPaymentForPeriod: Option[String] = None,
                                  noPaymentDates: Option[ClosedDateRange] = None,
                                  periodOfInactivity: Option[ClosedDateRange] = None,
                                  apprenticeshipLevy: Option[ApprenticeshipLevy] = None,
                                  relatedTaxYear: String,
                                  finalSubmission: Option[FinalSubmission] = None)

object EmployerPaymentSummary {
  implicit val ldtFormats = new Format[LocalDateTime] {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    override def reads(json: JsValue): JsResult[LocalDateTime] = implicitly[Reads[JsString]].reads(json).map { js =>
      fmt.parseDateTime(js.value).toLocalDateTime
    }
    // $COVERAGE-OFF$
    override def writes(o: LocalDateTime): JsValue = JsString(fmt.print(o))
    // $COVERAGE-ON$
  }

  implicit val drFormats = Json.format[ClosedDateRange]
  implicit val formats = Json.format[EmployerPaymentSummary]
}
