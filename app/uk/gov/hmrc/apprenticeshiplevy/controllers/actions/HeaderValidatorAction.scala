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

package uk.gov.hmrc.apprenticeshiplevy.controllers.actions

import play.api.mvc.{ActionBuilder, Request, Result}
import play.mvc.Http.HeaderNames.ACCEPT
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponse.ErrorAcceptHeaderInvalid
import scala.concurrent.Future


object HeaderValidatorAction extends ActionBuilder[Request] {

  private val ExpectedAcceptHeader = "application/vnd.hmrc.1.0+json"

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.headers.get(ACCEPT).contains(ExpectedAcceptHeader) match {
      case true => block(request)
      case false => Future.successful(ErrorAcceptHeaderInvalid.Result)
    }
  }
}
