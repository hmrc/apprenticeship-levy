/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.mvc.{BodyParsers, ControllerComponents}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.apprenticeshiplevy.connectors.LiveDesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.EmprefController
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.{AllProviderAuthActionImpl, AuthAction}
import uk.gov.hmrc.apprenticeshiplevy.data.api.{EmploymentReference, Nino}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class LiveEmprefController @Inject()(val desConnector: LiveDesConnector,
                                     val auth: AllProviderAuthActionImpl,
                                     val executionContext: ExecutionContext,
                                     val parser: BodyParsers.Default,
                                     val appContext: AppContext,
                                     val controllerComponents: ControllerComponents) extends EmprefController {

  override val authAction: EmploymentReference => AuthAction = auth(_)

  override def emprefUrl(empref: EmploymentReference): String = routes.LiveEmprefController.empref(empref).url

  override def declarationsUrl(empref: EmploymentReference): String = routes.LiveLevyDeclarationController.declarations(empref, None, None).url

  override def fractionsUrl(empref: EmploymentReference): String = routes.LiveFractionsController.fractions(empref, None, None).url

  override def employmentCheckUrl(empref: EmploymentReference): String =
    routes.LiveEmploymentCheckController.check(empref, Nino("nino"), LocalDate.now(), LocalDate.now())
      .url.replaceAll("\\?.*", "").replaceAll("nino", "{nino}")

}
