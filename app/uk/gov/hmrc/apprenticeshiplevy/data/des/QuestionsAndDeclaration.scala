/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json._

case class QuestionsAndDeclaration(freeOfTaxPaymentsMadeToEmployee: Boolean,
                                   expensesVouchersOrBenefitsFromOthers: Boolean,
                                   personEmployedOutsideUKWorkedFor30DaysOrMore: Boolean,
                                   payToSomeoneElse: Boolean,
                                   p11DFormsDue: Boolean,
                                   serviceCompany: Boolean)

object QuestionsAndDeclaration {
  implicit val format = Json.format[QuestionsAndDeclaration]
}
