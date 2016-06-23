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

package uk.gov.hmrc.apprenticeshiplevy.controllers

import play.api.hal.{Hal, HalLink, HalResource}
import uk.gov.hmrc.apprenticeshiplevy.connectors.AuthConnector
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

trait HalController extends ApiController {
  def authConnector: AuthConnector

  def rootUrl: String

  def emprefUrl(empref: String): String

  // Hook to allow post-processing of the links, specifically for sandbox handling
  def processLink(l: HalLink): HalLink = identity(l)

  def root = withValidAcceptHeader.async { implicit request =>
    authConnector.getEmprefs.map(es => Ok(transformEmpRefs(es)))
  }

  private[controllers] def transformEmpRefs(empRefs: Seq[String]): HalResource = {
    val links = selfLink(rootUrl) +: empRefs.map(empref => HalLink(empref, emprefUrl(empref)))

    Hal.linksSeq(links.map(processLink))
  }
}
