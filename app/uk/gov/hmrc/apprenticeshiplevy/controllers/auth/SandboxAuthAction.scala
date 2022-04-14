/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.controllers.auth

import com.google.inject.Inject
import play.api.mvc.{BodyParsers, Request, Result}
import uk.gov.hmrc.domain.EmpRef
import scala.concurrent.{ExecutionContext, Future}

class SandboxAuthAction @Inject()(val parser: BodyParsers.Default,
                                  val executionContext: ExecutionContext) extends AuthAction {
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    Future.successful(Right(AuthenticatedRequest(request, Some(EmpRef("840", "MODES17")))))
  }
}
