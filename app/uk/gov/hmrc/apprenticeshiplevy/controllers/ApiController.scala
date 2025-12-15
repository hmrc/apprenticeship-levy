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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.slf4j.MDC
import play.api.hal.{HalLink, HalResource, halResourceWrites}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.apprenticeshiplevy.utils.HeaderValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import scala.concurrent.ExecutionContext

trait ApiController extends BackendBaseController with HeaderValidator {

  implicit val ec: ExecutionContext = controllerComponents.executionContext

  implicit class ErrorResponseSyntax(er: ErrorResponse) {
    def result: Result = Status(er.statusCode)(Json.toJson(er))
  }

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier = {
    val hc = super.hc(using rh)
    MDC.put("X-Client-ID", rh.headers.toSimpleMap.getOrElse("X-Client-ID", "Unknown caller"))
    val headersMap = rh.headers.toSimpleMap
    val clientId = headersMap.getOrElse("X-Client-ID", "Unknown caller")
    val user = headersMap.getOrElse("X-Client-Authorization-Token", "Unknown caller")

    hc.copy(extraHeaders = Seq(("X-Client-ID", clientId), ("X-Client-Authorization-Token", user)) ++ hc.extraHeaders)
  }

  def selfLink(url: String): HalLink = HalLink("self", url)

  def ok(hal: HalResource): Result = Ok(Json.toJson(hal)).as("application/hal+json")

  protected val withValidAcceptHeader: ActionBuilder[Request, AnyContent] = validateAccept(acceptHeaderValidationRules)
}
