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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import play.api.hal.{HalLink, HalLinks, HalResource}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.apprenticeshiplevy.controllers.auth.AuthAction
import uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference
import uk.gov.hmrc.apprenticeshiplevy.utils.DecodePath.decodeAnyDoubleEncoding

trait RootController extends ApiController {

  def rootUrl: String

  val authAction: AuthAction

  def emprefUrl(empref: EmploymentReference): String

  // Hook to allow post-processing of the links, specifically for sandbox handling
  def processLink(l: HalLink): HalLink = identity(l)

  // scalastyle:off
  def root: Action[AnyContent] = (withValidAcceptHeader andThen authAction) { implicit request =>
    // scalastyle:on

    val empRef = if (request.empRef.isDefined) Seq(request.empRef.get.toString) else Seq.empty

    ok(transformEmpRefs(empRef))
  }

  private[controllers] def transformEmpRefs(empRefs: Seq[String]): HalResource = {
    val links = selfLink(decodeAnyDoubleEncoding(rootUrl)) +: empRefs.map(empref =>
      HalLink(empref, decodeAnyDoubleEncoding(emprefUrl(EmploymentReference(empref))))
    )
    val body = Json.toJson(Map("emprefs" -> empRefs)).as[JsObject]

    HalResource(HalLinks(links.map(processLink).toVector), body)
  }
}
