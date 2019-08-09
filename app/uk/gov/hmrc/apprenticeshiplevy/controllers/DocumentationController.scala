/*
 * Copyright 2019 HM Revenue & Customs
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
import java.io.{File, InputStream}

import com.google.inject.{Inject, Singleton}
import play.api.{Application, Mode, Play}
import play.api.Play.current
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.{Json, _}
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.Logger

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

//trait AssetsController extends BaseController {
//  implicit def current: Option[Application]
//
//  private val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r
//
//
//}
@Singleton
class DocumentationController extends BaseController {
  implicit val current = AppContext.maybeApp

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

  def retrieve(rootPath: String, file: String): Option[InputStream] = {
    current.flatMap { app =>
      if (app.mode == Mode.Prod) {
        // $COVERAGE-OFF$
        app.resourceAsStream(s"${rootPath}/${file}")
        // $COVERAGE-ON$
      } else {
        app.getExistingFile(s"${rootPath}/${file}").map(new java.io.FileInputStream(_))
      }
    }
  }

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    retrieve(rootPath, file) match {
      case Some(fileToServe) => {
        val mimeType = if (file.contains("raml")) "application/raml+yaml" else play.api.libs.MimeTypes.forFileName(file).getOrElse("text/plain")
        Ok(Source.fromInputStream(fileToServe).mkString).as(mimeType)
      }
      case _ => {
        // $COVERAGE-OFF$
        Logger.error(s"Assets controller failed to serve a file: ${rootPath}/${file}.")
        // $COVERAGE-ON$
        NotFound
      }
    }
  }

  def documentation(version: String, endpoint: String): Action[AnyContent]  =
    at(s"public/documentation/$version", s"${endpoint.replaceAll(" ", "-")}.xml")

  def definition: Action[AnyContent]  = Action.async {
    val filename = "definition.json"
    retrieve("public/api", filename) match {
      case Some(fileToServe) => {
        if (AppContext.privateModeEnabled) {
          enrichDefinition(fileToServe) match {
            case Success(json) => Future.successful(Ok(json).withHeaders(HeaderNames.CONTENT_TYPE->MimeTypes.JSON))
            case Failure(_) => Future.successful(InternalServerError)
          }
        } else {
          Future.successful(Ok(Source.fromInputStream(fileToServe).mkString).as(MimeTypes.JSON))
        }
      }
      case _ => {
        // $COVERAGE-OFF$
        Logger.error(s"Documentation controller failed to serve a file: public/api/definition.json as not found")
        // $COVERAGE-ON$
        Future.successful(NotFound)
      }
    }
  }

  def enrichDefinition(inputStream: InputStream): Try[JsValue] = {
    Json.parse(Source.fromInputStream(inputStream).mkString).transform(whitelistJsonTransformer) match {
      case JsSuccess(json, _) => Success(json)
      case JsError(_) => Failure(new RuntimeException("Unable to transform definition.json"))
    }
  }

  def conf(version: String, file: String): Action[AnyContent] = {
    at(s"public/api/conf/${version}", file)
  }
}

