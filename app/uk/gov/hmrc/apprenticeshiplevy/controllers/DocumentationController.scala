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

import java.io.InputStream

import controllers.AssetsBuilder
import play.api.libs.json.{Json, _}
import play.api.mvc.Action
import play.api.{Application, Play}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait DocumentationController extends AssetsBuilder with BaseController {

  implicit def current: Application

  lazy val whitelist = Json.obj(
    "access" -> Json.obj(
      "type" -> "PRIVATE",
      "whitelistedApplicationIds" -> JsArray(List(JsString("f0e2611e-2f45-4326-8cd2-6eefebec77b7")))
    ))

  lazy val whitelistJsonTransformer = (__ \ 'api \ 'versions).json.update(
    __.read[JsArray].map { versions =>
      JsArray(versions.value.updated(0, versions(0).as[JsObject] ++ whitelist))
    })

  def documentation(version: String, endpoint: String) =
    super.at(s"/public/documentation/$version", s"${endpoint.replaceAll(" ", "-")}.xml")

  def definition =
    if (AppContext.privateModeEnabled) {
      Action { implicit request =>
        Play.resourceAsStream("public/api/definition.json")
          .map(enrichDefinition)
          .map {
            case Success(json) => Ok(json)
            case Failure(_) => InternalServerError
          }
          .getOrElse(NotFound)
      }
    }
    else super.at(s"/public/api", "definition.json")

  def enrichDefinition(inputStream: InputStream): Try[JsValue] =
    Json.parse(Source.fromInputStream(inputStream).mkString).transform(whitelistJsonTransformer) match {
      case JsSuccess(json, _) => Success(json)
      case JsError(_) => Failure(new RuntimeException("Unable to transform definition.json"))
    }
}

object DocumentationController extends DocumentationController {
  override implicit val current = Play.current
}
