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

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import play.api.libs.json._
import play.api.libs.json.Json._
import org.joda.time.LocalDate
import org.joda.time.DateTimeConstants.{APRIL, MAY}
import org.joda.time.{LocalDate, LocalDateTime}
import uk.gov.hmrc.apprenticeshiplevy.utils.ClosedDateRange
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import scala.util.{Try, Success, Failure}

class EmployerPaymentSummarySpec extends UnitSpec {
  "convertToDeclaration" should {
    val id = 123456L
    val submissionTime = LocalDateTime.now()
    val rtiSubmissionTime = LocalDateTime.now()
    val relatedTaxYear = "16-17"

    "convert a terminated payroll scheme" in {
      Try(AppContext.metricsEnabled) match {
        case Success(v) => info(s"Metrics enabled: ${v}")
        case Failure(e) => e.printStackTrace()
      }
      val schemeCeasedDate = submissionTime.minusDays(1).toLocalDate
      val eps = EmployerPaymentSummary(id,
                                       submissionTime,
                                       rtiSubmissionTime,
                                       relatedTaxYear,
                                       finalSubmission = Some(SchemeCeased(true, schemeCeasedDate, None)))

      EmployerPaymentSummary.toDeclarations(eps)(0) shouldBe LevyDeclaration(id, submissionTime, Some(schemeCeasedDate))
    }

    "convert an inactive submission" in {
      val inactiveFrom = submissionTime.minusMonths(2).toLocalDate
      val inactiveTo = submissionTime.minusMonths(2).toLocalDate
      val eps = EmployerPaymentSummary(id,
                                       submissionTime,
                                       rtiSubmissionTime,
                                       relatedTaxYear,
                                       inactivePeriod = Some(ClosedDateRange(from = inactiveFrom, to = inactiveTo)))

      EmployerPaymentSummary.toDeclarations(eps)(0) shouldBe LevyDeclaration(id, submissionTime, inactiveFrom = Some(inactiveFrom), inactiveTo = Some(inactiveTo))
    }

    "convert a submission" in {
      val levyDueYTD = BigDecimal(500)
      val allowance = BigDecimal(10000)
      val taxMonth = 2
      val eps = EmployerPaymentSummary(id,
                                       submissionTime,
                                       rtiSubmissionTime,
                                       relatedTaxYear,
                                       apprenticeshipLevy = Some(ApprenticeshipLevy(levyDueYTD, allowance, taxMonth.toString())))

      EmployerPaymentSummary.toDeclarations(eps)(0) shouldBe LevyDeclaration(id, submissionTime,
        payrollPeriod = Some(PayrollPeriod(relatedTaxYear, taxMonth)),
        levyDueYTD = Some(levyDueYTD),
        levyAllowanceForFullYear = Some(allowance))
    }

    "convert a submission with no payment for period" in {
      val startNoPayment = new LocalDate("2016-05-06")
      val endNoPayment = new LocalDate("2016-06-05")

      val expectedTaxMonth = 2

      val eps = EmployerPaymentSummary(id,
                                       submissionTime,
                                       rtiSubmissionTime,
                                       relatedTaxYear,
                                       Some(ClosedDateRange(startNoPayment, endNoPayment)))

      EmployerPaymentSummary.toDeclarations(eps)(0) shouldBe LevyDeclaration(id, submissionTime,
        noPaymentForPeriod = Some(true),
        payrollPeriod = Some(PayrollPeriod(year = "16-17", month = expectedTaxMonth)))
    }
  }

  "calculate tax month" should {
    "calculate month 1 for 6 Apr" in {
      EmployerPaymentSummary.calculateTaxMonth(new LocalDate(2016, APRIL, 6)) shouldBe 1
    }

    "calculate month 1 for 5 May" in {
      EmployerPaymentSummary.calculateTaxMonth(new LocalDate(2016, MAY, 5)) shouldBe 1
    }

    "calculate month 2 for 6 May" in {
      EmployerPaymentSummary.calculateTaxMonth(new LocalDate(2016, MAY, 5)) shouldBe 1
    }

    "calculate month 12 for 5 Apr" in {
      EmployerPaymentSummary.calculateTaxMonth(new LocalDate(2016, APRIL, 5)) shouldBe 12
    }
  }
}