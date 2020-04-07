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

import com.google.inject.Inject
import uk.gov.hmrc.apprenticeshiplevy.connectors.SandboxDesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.SandboxPrivilegedAuthAction
import uk.gov.hmrc.apprenticeshiplevy.controllers.{DesController, LevyDeclarationController}

class SandboxLevyDeclarationController @Inject()(val desConnector: SandboxDesConnector,
                                                 val authAction: SandboxPrivilegedAuthAction) extends DesController with LevyDeclarationController
