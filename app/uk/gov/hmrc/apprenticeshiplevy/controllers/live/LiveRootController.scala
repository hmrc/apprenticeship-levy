/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.apprenticeshiplevy.connectors.{AuthConnector, LiveAuthConnector}
import uk.gov.hmrc.apprenticeshiplevy.controllers.RootController
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference

object LiveRootController extends RootController {
  override def authConnector: AuthConnector = LiveAuthConnector

  override def rootUrl: String = routes.LiveRootController.root().url

  override def emprefUrl(empref: EmploymentReference): String = routes.LiveEmprefController.empref(empref).url
}
