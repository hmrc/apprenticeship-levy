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

package uk.gov.hmrc.apprenticeshiplevy.controllers.live

import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.controllers.EmploymentCheckController
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotImplemented

object LiveEmploymentCheckController extends EmploymentCheckController {

  override def check(empref: String, nino: String, atDate: Option[LocalDate]) = withValidAcceptHeader {
    ErrorNotImplemented.result
  }

  override def rtiConnector = ???
}