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
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.PrivilegedAuthActionImpl
import uk.gov.hmrc.apprenticeshiplevy.controllers.{DesController, FractionsCalculationDateController, FractionsController}

import scala.concurrent.ExecutionContext

class LiveFractionsController @Inject()(val desConnector: LiveDesConnector,
                                        val authAction: PrivilegedAuthActionImpl,
                                        val executionContext: ExecutionContext,
                                        val parser: BodyParsers.Default,
                                        val appContext: AppContext,
                                        val controllerComponents: ControllerComponents) extends DesController with FractionsController

class LiveFractionsCalculationDateController  @Inject()(val desConnector: LiveDesConnector,
                                                        val authAction: PrivilegedAuthActionImpl,
                                                        val executionContext: ExecutionContext,
                                                        val parser: BodyParsers.Default,
                                                        val appContext: AppContext,
                                                        val controllerComponents: ControllerComponents) extends DesController with FractionsCalculationDateController
