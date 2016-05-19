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
import uk.gov.hmrc.domain.EmpRef

object LevyData {
  val declarations =
    List(
      LevyDeclarations("123/AB12345", List(
        LevyDeclaration(PayrollMonth(2016, 2), BigDecimal(-200), "amended", "2016-03-15", EnglishFraction(BigDecimal(0.79), new LocalDate(2016, 2, 5))),
        LevyDeclaration(PayrollMonth(2016, 2), BigDecimal(1000), "original", "2016-02-21", EnglishFraction(BigDecimal(0.83), new LocalDate(2015, 4, 5))),
        LevyDeclaration(PayrollMonth(2016, 1), BigDecimal(500), "original", "2016-01-21", EnglishFraction(BigDecimal(0.83), new LocalDate(2015, 4, 5))),
        LevyDeclaration(PayrollMonth(2015, 12), BigDecimal(600), "original", "2015-12-21", EnglishFraction(BigDecimal(0.83), new LocalDate(2015, 4, 5)))
      ))
    )

  val data: Map[String, List[LevyDeclarations]] = declarations.groupBy(_.empref)

}
