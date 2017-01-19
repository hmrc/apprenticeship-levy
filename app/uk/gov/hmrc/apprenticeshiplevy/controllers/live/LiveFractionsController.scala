/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.apprenticeshiplevy.connectors.{DesConnector, LiveDesConnector}
import uk.gov.hmrc.apprenticeshiplevy.controllers.{DesController, FractionsCalculationDateController, FractionsController}

object LiveFractionsController extends DesController with FractionsController {
  override def desConnector: DesConnector = LiveDesConnector
}

object LiveFractionsCalculationDateController extends DesController with FractionsCalculationDateController {
  override def desConnector: DesConnector = LiveDesConnector
}
