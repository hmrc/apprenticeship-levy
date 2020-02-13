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

package uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox

import play.api.Play
import uk.gov.hmrc.apprenticeshiplevy.connectors.{DesConnector, SandboxDesConnector}
import uk.gov.hmrc.apprenticeshiplevy.controllers.EmploymentCheckController
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AuthAction, SandboxPrivilegedAuthAction}

object SandboxEmploymentCheckController extends EmploymentCheckController {
  override def desConnector: DesConnector = SandboxDesConnector
  override val authAction: AuthAction = Play.current.injector.instanceOf[SandboxPrivilegedAuthAction]
}
