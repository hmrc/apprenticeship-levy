/*
 * Copyright 2019 HM Revenue & Customs
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
import org.joda.time.DateTimeConstants.APRIL
import org.joda.time.Months.monthsBetween
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.data.des.FinalSubmission._
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import scala.util.{Try, Success, Failure}

case class EmployerPaymentSummary(submissionId: Long,
                                  hmrcSubmissionTime: LocalDateTime,
                                  rtiSubmissionTime: LocalDateTime,
                                  taxYear: String,
                                  noPaymentPeriod: Option[ClosedDateRange] = None,
                                  inactivePeriod: Option[ClosedDateRange] = None,
                                  employmentAllowanceInd: Option[Boolean] = None,
                                  apprenticeshipLevy: Option[ApprenticeshipLevy] = None,
                                  finalSubmission: Option[FinalSubmission] = None,
                                  questionsAndDeclarations: Option[QuestionsAndDeclaration] = None)

object EmployerPaymentSummary {
  val TAX_YEAR_START_DAY = 6
  val BeginningOfTaxYear = new MonthDay(APRIL, TAX_YEAR_START_DAY)

  private[des] def calculateTaxMonth(to: LocalDate) = {
    val monthDay = new MonthDay(to.getMonthOfYear, to.getDayOfMonth)
    val yearReference = if (monthDay.isBefore(BeginningOfTaxYear)) to.getYear - 1 else to.getYear
    monthsBetween(new LocalDate(yearReference, APRIL, TAX_YEAR_START_DAY), to).getMonths + 1
  }

  private[des] val toNoPayment: PartialFunction[EmployerPaymentSummary, LevyDeclaration] = {
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, Some(dr), _, _, _, _, _) =>
      LevyDeclaration((id * 10L),
                      hmrcSt,
                      payrollPeriod = Some(PayrollPeriod(ty, calculateTaxMonth(dr.to))),
                      noPaymentForPeriod = Some(true),
                      submissionId = id)
  }

  private[des] val toInactive: PartialFunction[EmployerPaymentSummary, LevyDeclaration] = {
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, Some(dr), _, _, _, _) =>
      LevyDeclaration(((id * 10L) + 1L),
                      hmrcSt,
                      inactiveFrom = Some(dr.from),
                      inactiveTo = Some(dr.to),
                      submissionId = id)
  }

  private[des] val toLevyDeclaration: PartialFunction[EmployerPaymentSummary, LevyDeclaration] = {
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, _, _, Some(al), _, _) =>
      LevyDeclaration(((id * 10L) + 2L),
                      hmrcSt,
                      payrollPeriod = Some(PayrollPeriod(ty, al.taxMonth.toInt)),
                      levyDueYTD=Some(al.amountDue),
                      levyAllowanceForFullYear=Some(al.amountAllowance),
                      submissionId = id)
  }

  private[des] val toCeased: PartialFunction[EmployerPaymentSummary, LevyDeclaration] = {
    case EmployerPaymentSummary(id, hmrcSt, rtiSt, ty, _, _, _, _, Some(SchemeCeased(_, schemeCeasedDate, _)), _) =>
      LevyDeclaration(((id * 10L) + 3L),
                      hmrcSt,
                      dateCeased = Some(schemeCeasedDate),
                      submissionId = id)
  }

  val conversions = Seq(toNoPayment,toInactive,toLevyDeclaration,toCeased)

  def toDeclarations(eps: EmployerPaymentSummary): Seq[LevyDeclaration] = convert[EmployerPaymentSummary,LevyDeclaration](conversions)(eps)

  def convert[T,U](partialFunctions: Seq[PartialFunction[T,U]])(t: T): Seq[U] =
    partialFunctions.foldLeft(Seq.empty[U]) { (seq, pf) =>
      Try(pf(t)) match {
        case Success(converted) => seq :+ converted
        case Failure(_) => seq
      }
    }

  implicit val dateRangeFormat = Json.format[ClosedDateRange]

  implicit val jodaDateTimeFormat = new Format[LocalDateTime] {
    val localDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
    val DateTime = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}){1}(.*)".r

    override def reads(json: JsValue): JsResult[LocalDateTime] = implicitly[Reads[JsString]].reads(json).map { js =>
      js.value match {
        case DateTime(timestamp,_) => localDateTimeFormat.parseDateTime(timestamp).toLocalDateTime
        case _ => {
          play.api.Logger.warn(s"Bad date time value of '${js.value}' returned from DES so returning new LocalDateTime(0L)")
          new LocalDateTime(0L)
        }
      }

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

abstract class EPSResponse {}

case class EmployerPaymentsSummary(empref: String, eps: List[EmployerPaymentSummary]) extends EPSResponse

object EmployerPaymentsSummary {
  implicit val format = Json.format[EmployerPaymentsSummary]
}

case class EmployerPaymentsSummaryVersion0(empref: String, declarations: List[EmployerPaymentSummary]) extends EPSResponse

object EmployerPaymentsSummaryVersion0 {
  implicit val format = Json.format[EmployerPaymentsSummaryVersion0]
}

case class EmptyEmployerPayments(empref: String) extends EPSResponse

object EmptyEmployerPayments {
  implicit val format = Json.format[EmptyEmployerPayments]
}

case class EmployerPaymentsError(reason: String) extends EPSResponse

object EmployerPaymentsError {
  implicit val format = Json.format[EmployerPaymentsError]
}
