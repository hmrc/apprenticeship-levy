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

import org.joda.time.LocalDate
import play.api.libs.json.Json

case class PayrollMonth(year: Int, month: Int) {
  // TODO: this should be removed
  def startDate = new LocalDate(year, month, 6)
  def endDate = new LocalDate(year, month, 5).plusMonths(1)
}

object PayrollMonth {
  implicit val formats = Json.format[PayrollMonth]
}

case class LevyDeclaration(payrollMonth: PayrollMonth, amount: BigDecimal, submissionType: String, submissionDate: String)

object LevyDeclaration {
  implicit val formats = Json.format[LevyDeclaration]
}

case class LevyDeclarations(empref: String, schemeCessationDate: Option[LocalDate], declarations: Seq[LevyDeclaration])

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