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

import java.net.URLEncoder

import play.api.hal.{Hal, HalLink}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.apprenticeshiplevy.connectors.SandboxAuthConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.ApiController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

object SandboxController extends ApiController {

  def root = withValidAcceptHeader.async { implicit request =>
    SandboxAuthConnector.getEmprefs.map(es => Ok(transformEmpRefs(es)))
  }

  private def transformEmpRefs(empRefs: Seq[String]): JsValue = {
    val url = uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.routes.SandboxController.root().url

    val links = empRefs.map(empref => {
      val encoded_er = URLEncoder.encode(empref, "UTF-8")
      HalLink(empref, s"$url/$encoded_er")
    })

    val self = selfLink(url)
    Json.toJson(Hal.hal(Json.toJson(JsObject(Seq())), (self +: links).toVector))
  }

  private def selfLink(url: String) = HalLink("self", url)
}
