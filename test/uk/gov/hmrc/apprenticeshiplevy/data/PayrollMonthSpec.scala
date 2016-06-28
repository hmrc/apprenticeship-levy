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
import org.scalatest.{FlatSpec, Matchers}

class PayrollMonthSpec extends FlatSpec with Matchers {

  "Payroll month" should "calculate correct start date" in {
    val data = Seq(
      (PayrollMonth(2016, 1), new LocalDate(2016, 1, 6)),
      (PayrollMonth(2016, 2), new LocalDate(2016, 2, 6)),
      (PayrollMonth(2015, 4), new LocalDate(2015, 4, 6)),
      (PayrollMonth(2018, 7), new LocalDate(2018, 7, 6)),
      (PayrollMonth(2020, 9), new LocalDate(2020, 9, 6)),
      (PayrollMonth(2017, 4), new LocalDate(2017, 4, 6))
    )

    data.foreach { case (pm, d) => pm.startDate shouldBe d }
  }

  it should "calculate correct end date" in {
    val data = Seq(
      (PayrollMonth(2016, 1), new LocalDate(2016, 2, 5)),
      (PayrollMonth(2016, 2), new LocalDate(2016, 3, 5)),
      (PayrollMonth(2015, 4), new LocalDate(2015, 5, 5)),
      (PayrollMonth(2018, 7), new LocalDate(2018, 8, 5)),
      (PayrollMonth(2020, 9), new LocalDate(2020, 10, 5)),
      (PayrollMonth(2017, 12), new LocalDate(2018, 1, 5))
    )

    data.foreach { case (pm, d) => pm.endDate shouldBe d }
  }

  it should "calculate correct PayrollMonth" in {
    val data = Seq(
      (new LocalDate(2016, 1, 1), PayrollMonth(2015, 9)),
      (new LocalDate(2016, 2, 5), PayrollMonth(2015, 10)),
      (new LocalDate(2016, 2, 6), PayrollMonth(2015, 11)),
      (new LocalDate(2016, 4, 5), PayrollMonth(2015, 12)),
      (new LocalDate(2016, 4, 6), PayrollMonth(2016, 1))
    )

    data.foreach { case (d, pm) => PayrollMonth.forDate(d) shouldBe pm }
  }

}
