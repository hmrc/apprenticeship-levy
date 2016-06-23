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

package uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox

import play.api.hal.HalLink
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.SandboxAuthConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.HalController

trait SandboxHalController extends HalController with SandboxLinkHelper {
  override val rootUrl: String = uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxHalController.root().url

  override def emprefUrl(empref: String): String = uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxEmprefRoutesController.routes(empref).url

  override def processLink(l: HalLink): HalLink = stripSandboxForNonDev(l)
}


object SandboxHalController extends SandboxHalController {
  override val env = AppContext.env

  override def authConnector = SandboxAuthConnector
}

