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

package uk.gov.hmrc.apprenticeshiplevy.config

import play.api.mvc._
import play.api.routing._

import javax.inject.Inject

import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

trait IsInExternalTest {
  def isInExternalTest: Boolean
}

object ConditionalRouter extends ConditionalRouter with IsInExternalTest {
  def isInExternalTest: Boolean = AppContext.externalTestModeEnabled
}

trait ConditionalRouter extends SimpleRouter with IsInExternalTest
{
  override def routes: Routes = {
    isInExternalTest match {
      case true => {
        play.api.Logger.debug("In External Test Environment: Using externaltest.routes")
        externaltest.Routes.routes
      }
      case false => {
        play.api.Logger.debug("Not in External Test Environment: Using nonexternaltest.routes")
        nonexternaltest.Routes.routes
      }
    }
  }
}
