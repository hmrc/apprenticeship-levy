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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.slf4j.MDC
import play.api.hal.{HalLink, HalResource}
import play.api.libs.json.Json
import play.api.mvc.{ActionBuilder, AnyContent, ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.api.controllers.{ErrorResponse, HeaderValidator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.util.Try

trait ApiController extends BackendController with HeaderValidator {

  def cc: ControllerComponents

  implicit class ErrorResponseSyntax(er: ErrorResponse) {
    def result: Result = Status(er.httpStatusCode)(Json.toJson(er))
  }

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier = {
    val hc = super.hc(rh)
    updateMDC
    val headersMap = rh.headers.toSimpleMap
    val clientId = headersMap.getOrElse("X-Client-ID","Unknown caller")
    val user = headersMap.getOrElse("X-Client-Authorization-Token","Unknown caller")

    hc.copy(extraHeaders = Seq(("X-Client-ID",clientId),("X-Client-Authorization-Token",user)) ++ hc.extraHeaders)
  }

  def selfLink(url: String): HalLink = HalLink("self", url)

  def ok(hal: HalResource): Result = Ok(Json.toJson(hal)).as("application/hal+json")

  protected def updateMDC(implicit rh: RequestHeader): Unit = {
    MDC.put("X-Client-ID",rh.headers.toSimpleMap.getOrElse("X-Client-ID","Unknown caller"))
    MDC.put("Authorization",rh.headers.toSimpleMap.getOrElse("X-Client-Authorization-Token","Unknown caller"))
  }

  protected val withValidAcceptHeader: ActionBuilder[Request, AnyContent] = validateAccept(acceptHeaderValidationRules)

  protected def extractReason(msg: String) =
    Try(if (msg.contains("Response body")) {
      val str1 = msg.reverse.substring(1).reverse.substring(msg.indexOf("Response body") + 14).trim
      val m = if (str1.startsWith("{")) str1 else str1.substring(str1.indexOf("{"))
      Try((Json.parse(m) \ "reason").as[String]) getOrElse ((Json.parse(m) \ "Reason").as[String])
    } else {msg}) getOrElse(msg)
}
