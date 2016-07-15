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

import org.joda.time.DateTimeConstants.{APRIL, MAY}
import org.joda.time.{DateTimeConstants, LocalDate, LocalDateTime}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.connectors._
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveLevyDeclarationController
import uk.gov.hmrc.apprenticeshiplevy.data.{LevyDeclaration, PayrollPeriod}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec

class LevyDeclarationControllerSpec extends UnitSpec with ScalaFutures {
  "getting the levy declarations" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = LiveLevyDeclarationController.declarations("empref", None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }

    "return an HTTP Not Implemented response" in {
      val response = LiveLevyDeclarationController.declarations("empref", None)(FakeRequest().withHeaders("Accept" -> "application/vnd.hmrc.1.0+json")).futureValue
      response.header.status shouldBe NOT_IMPLEMENTED
    }
  }

  "convertToDeclaration" should {

    val id = 123456L
    val submissionTime = LocalDateTime.now()
    val relatedTaxYear = "16-17"

    val refs = EmpRefs(officeNumber = "123", payeRef = "123/AB12345", aoRef = "123PQ7654321X")

    "convert a terminated payroll scheme" in {
      val schemeCeasedDate = submissionTime.minusDays(1).toLocalDate
      val eps = EmployerPaymentSummary(id, submissionTime, refs, finalSubmission = Some(FinalSubmission(Some("yes"), dateSchemeCeased = Some(schemeCeasedDate))), relatedTaxYear = relatedTaxYear)

      TestLevyDeclarationController.convertToDeclaration(eps) shouldBe LevyDeclaration(id, submissionTime, Some(schemeCeasedDate))
    }

    "convert an inactive submission" in {
      val inactiveFrom = submissionTime.minusMonths(2).toLocalDate
      val inactiveTo = submissionTime.minusMonths(2).toLocalDate
      val eps = EmployerPaymentSummary(id, submissionTime, refs, relatedTaxYear = relatedTaxYear, periodOfInactivity = Some(DateRange(from = inactiveFrom, to = inactiveTo)))

      TestLevyDeclarationController.convertToDeclaration(eps) shouldBe LevyDeclaration(id, submissionTime, inactiveFrom = Some(inactiveFrom), inactiveTo = Some(inactiveTo))
    }

    "convert a submission" in {
      val levyDueYTD = BigDecimal(500)
      val allowance = BigDecimal(10000)
      val taxMonth = 2
      val eps = EmployerPaymentSummary(id, submissionTime, refs, relatedTaxYear = relatedTaxYear, apprenticeshipLevy = Some(ApprenticeshipLevy(levyDueYTD, taxMonth, allowance)))

      TestLevyDeclarationController.convertToDeclaration(eps) shouldBe LevyDeclaration(id, submissionTime,
        payrollPeriod = Some(PayrollPeriod(relatedTaxYear, taxMonth)),
        levyDueYTD = Some(levyDueYTD),
        allowance = Some(allowance))
    }

    "convert a submission with no payment for period" in {

      val startNoPayment = new LocalDate("2016-05-06")
      val endNoPayment = new LocalDate("2016-06-05")

      val expectedTaxMonth = 2

      val eps = EmployerPaymentSummary(id, submissionTime, refs, Some("yes"), Some(DateRange(startNoPayment, endNoPayment)), relatedTaxYear = relatedTaxYear)

      TestLevyDeclarationController.convertToDeclaration(eps) shouldBe LevyDeclaration(id, submissionTime,
        noPaymentForPeriod = Some(true),
        payrollPeriod = Some(PayrollPeriod(year = "16-17", month = expectedTaxMonth)))
    }
  }

  "calculate tax month" should {
    "calculate month 1 for 6 Apr" in {
      TestLevyDeclarationController.calculateTaxMonth(new LocalDate(2016, APRIL, 6)) shouldBe 1
    }

    "calculate month 1 for 5 May" in {
      TestLevyDeclarationController.calculateTaxMonth(new LocalDate(2016, MAY, 5)) shouldBe 1
    }

    "calculate month 2 for 6 May" in {
      TestLevyDeclarationController.calculateTaxMonth(new LocalDate(2016, MAY, 5)) shouldBe 1
    }

    "calculate month 12 for 5 Apr" in {
      TestLevyDeclarationController.calculateTaxMonth(new LocalDate(2016, APRIL, 5)) shouldBe 12
    }
  }
}

object TestRTIConnector extends RTIConnector {
  override def rtiBaseUrl: String = ???

  override def httpGet: HttpGet = ???

  override def eps(empref: String, months: Option[Int])(implicit hc: HeaderCarrier) = ???
}

object TestLevyDeclarationController extends LevyDeclarationController with ApiController {
  override def rtiConnector: RTIConnector = TestRTIConnector
}
