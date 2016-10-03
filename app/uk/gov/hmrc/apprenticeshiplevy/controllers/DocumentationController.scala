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

import scala.io.Source
import java.io.File

import play.api.{Application, Play, Logger}
import play.api.libs.json.{Json, _}
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}

trait AssetsController extends BaseController {
  implicit def current: Application

  private val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  protected def retrieve(rootPath: String, file: String): File = {
    rootPath match {
      case AbsolutePath(_) => new File(rootPath, file)
      case _ => new File(current.getFile(rootPath), file)
    }
  }

  protected def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    val fileToServe = retrieve(rootPath, file)

    if (!file.isEmpty && fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      Logger.error("Assets controller failed to serve a file: " + file)
      NotFound
    }
  }
}

trait DocumentationController extends AssetsController {
  lazy val whitelistedApplicationIds = AppContext.whitelistedApplicationIds

  lazy val whitelist = Json.obj(
    "access" -> Json.obj(
      "type" -> "PRIVATE",
      "whitelistedApplicationIds" -> JsArray(whitelistedApplicationIds.map(JsString))
    ))

  lazy val whitelistJsonTransformer = (__ \ 'api \ 'versions).json.update(
    __.read[JsArray].map { versions =>
      JsArray(versions.value.updated(0, versions(0).as[JsObject] ++ whitelist))
    })

  def documentation(version: String, endpoint: String) =
    super.at(s"public/documentation/$version", s"${endpoint.replaceAll(" ", "-")}.xml")

  def definition = Action.async {
    val fileToServe = retrieve("public/api", "definition.json")

    if (fileToServe.exists) {
      if (AppContext.privateModeEnabled) {
        enrichDefinition(fileToServe) match {
          case Success(json) => Future.successful(Ok(json))
          case Failure(_) => Future.successful(InternalServerError)
        }
      } else {
        Future.successful(Ok.sendFile(fileToServe, inline = true))
      }
    } else {
      Logger.error(s"Documentation controller failed to serve a file: $fileToServe")
      Future.successful(NotFound)
    }
  }

  def enrichDefinition(file: File): Try[JsValue] = {
    Json.parse(Source.fromFile(file).getLines.mkString).transform(whitelistJsonTransformer) match {
      case JsSuccess(json, _) => Success(json)
      case JsError(_) => Failure(new RuntimeException("Unable to transform definition.json"))
    }
  }
}

object DocumentationController extends DocumentationController {
  override implicit val current = Play.current
}
