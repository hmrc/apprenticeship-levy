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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.data.des.FinalSubmission._

case class EmployerPaymentSummary(submissionId: Long,
                                  hmrcSubmissionTime: LocalDateTime,
                                  rtiSubmissionTime: LocalDateTime,
                                  taxYear: String,
                                  noPaymentPeriod: Option[ClosedDateRange],
                                  inactivePeriod: Option[ClosedDateRange],
                                  employmentAllowanceInd: Option[Boolean],
                                  apprenticeshipLevy: Option[ApprenticeshipLevy],
                                  finalSubmission: Option[FinalSubmission],
                                  questionsAndDeclarations: Option[QuestionsAndDeclaration])

object EmployerPaymentSummary {
  implicit val dateRangeFormat = Json.format[ClosedDateRange]

  implicit val jodaDateTimeFormat = new Format[LocalDateTime] {
    val localDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")

    override def reads(json: JsValue): JsResult[LocalDateTime] = implicitly[Reads[JsString]].reads(json).map { js =>
      localDateTimeFormat.parseDateTime(js.value).toLocalDateTime
    }
    // $COVERAGE-OFF$
    override def writes(date: LocalDateTime): JsValue = JsString(localDateTimeFormat.print(date))
    // $COVERAGE-ON$
  }

  implicit val epsWrites: Writes[EmployerPaymentSummary] = (
    (JsPath \ "submissionId").write[Long] and
    (JsPath \ "hmrcSubmissionTime").write[LocalDateTime] and
    (JsPath \ "rtiSubmissionTime").write[LocalDateTime] and
    (JsPath \ "taxYear").write[String] and
    (JsPath \ "noPaymentPeriod").write[Option[ClosedDateRange]] and
    (JsPath \ "inactivePeriod").write[Option[ClosedDateRange]] and
    (JsPath \ "employmentAllowanceInd").write[Option[Boolean]] and
    (JsPath \ "apprenticeshipLevy").write[Option[ApprenticeshipLevy]] and
    (JsPath \ "finalSubmission").write[Option[FinalSubmission]] and
    (JsPath \ "questionsAndDeclarations").write[Option[QuestionsAndDeclaration]]
  )(unlift(EmployerPaymentSummary.unapply))

  implicit val epsReads: Reads[EmployerPaymentSummary] = (
    (JsPath \ "submissionId").read[Long] and
    (JsPath \ "hmrcSubmissionTime").read[LocalDateTime] and
    (JsPath \ "rtiSubmissionTime").read[LocalDateTime] and
    (JsPath \ "taxYear").read[String] and
    (JsPath \ "noPaymentPeriod").readNullable[ClosedDateRange] and
    (JsPath \ "inactivePeriod").readNullable[ClosedDateRange] and
    (JsPath \ "employmentAllowanceInd").readNullable[Boolean] and
    (JsPath \ "apprenticeshipLevy").readNullable[ApprenticeshipLevy] and
    (JsPath \ "finalSubmission").readNullable[FinalSubmission] and
    (JsPath \ "questionsAndDeclarations").readNullable[QuestionsAndDeclaration]
  )(EmployerPaymentSummary.apply _)
}

case class EmployerPaymentsSummary(empref: String, eps: List[EmployerPaymentSummary])

object EmployerPaymentsSummary {
  implicit val format = Json.format[EmployerPaymentsSummary]
}
