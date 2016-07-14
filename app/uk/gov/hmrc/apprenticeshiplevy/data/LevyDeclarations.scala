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

package uk.gov.hmrc.apprenticeshiplevy.data

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.Json

case class PayrollPeriod(year: String, month: Int)

object PayrollPeriod {
  implicit val formats = Json.format[PayrollPeriod]
}

case class LevyDeclaration(id: Long,
                           submissionTime: DateTime,
                           dateCeased: Option[LocalDate] = None,
                           inactiveFrom: Option[LocalDate] = None,
                           inactiveTo: Option[LocalDate] = None,
                           payrollPeriod: Option[PayrollPeriod] = None,
                           amount: Option[BigDecimal] = None,
                           allowance: Option[BigDecimal] = None,
                           noPaymentForPeriod: Option[Boolean] = None)


object LevyDeclaration {
  implicit val formats = Json.format[LevyDeclaration]
}

case class LevyDeclarations(empref: String, declarations: Seq[LevyDeclaration])

object LevyDeclarations {
  implicit val formats = Json.format[LevyDeclarations]
}

case class Fraction(region: String, value: BigDecimal)

object Fraction {
  implicit val formats = Json.format[Fraction]
}

case class FractionCalculation(calculatedAt: LocalDate, fractions: Seq[Fraction])

object FractionCalculation {
  implicit val formats = Json.format[FractionCalculation]
}

case class Fractions(empref: String, fractionCalculations: Seq[FractionCalculation])

object Fractions {
  implicit val formats = Json.format[Fractions]
}