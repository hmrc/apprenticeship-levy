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

package uk.gov.hmrc.apprenticeshiplevy.config

import play.api.Logging

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

trait IsInExternalTest {
  def isInExternalTest: Boolean
}

class ConditionalRouter @Inject() (externalTestRoutes: externaltest.Routes,
                                   nonexternaltestRoutes: nonexternaltest.Routes,
                                   appContext: AppContext) extends SimpleRouter with IsInExternalTest with Logging {
  def isInExternalTest: Boolean = appContext.externalTestModeEnabled

  override def routes: Routes = {
    isInExternalTest match {
      case true => {
        logger.debug("In External Test Environment: Using externaltest.routes")
        externalTestRoutes.routes
      }
      case false => {
        logger.debug("Not in External Test Environment: Using nonexternaltest.routes")
        nonexternaltestRoutes.routes
      }
    }
  }
}
