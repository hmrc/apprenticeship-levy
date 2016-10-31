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


import play.api.Logger
import play.api.hal.{Hal, HalLink, HalResource}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.apprenticeshiplevy.connectors.EpayeConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.ErrorResponses.ErrorNotFound
import uk.gov.hmrc.play.http.NotFoundException
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

trait EmprefController extends ApiController {
  def epayeConnector: EpayeConnector

  def declarationsUrl(empref: String): String

  def fractionsUrl(empref: String): String

  def employmentCheckUrl(empref: String): String

  def emprefUrl(empref: String): String

  // Hook to allow post-processing of the links, specifically for sandbox handling
  def processLink(l: HalLink): HalLink = identity(l)

  // scalastyle:off
  def empref(empref: String) = withValidAcceptHeader.async { implicit request =>
  // scalastyle:on
    epayeConnector.designatoryDetails(empref).map { details =>
      val hal = prepareLinks(empref)
      ok(hal.copy(state = Json.toJson(details).as[JsObject]))
    }.recover {
      case e: NotFoundException => ErrorNotFound.result
    }
  }

  private[controllers] def prepareLinks(empref: String): HalResource = {
    val links = Seq(
      selfLink(emprefUrl(empref)),
      HalLink("declarations", declarationsUrl(empref)),
      HalLink("fractions", fractionsUrl(empref)),
      HalLink("employment-check", employmentCheckUrl(empref))
    )
    Hal.linksSeq(links.map(processLink))
  }
}
