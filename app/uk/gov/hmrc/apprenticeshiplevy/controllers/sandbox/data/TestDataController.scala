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

package uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.data

import play.api.libs.json._
import play.api.mvc._
import play.api.{Logger, Mode}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import uk.gov.hmrc.play.microservice.controller.Utf8MimeTypes
import java.net.URLDecoder
import play.api.Play.current
import scala.io.Source
import java.io.{File,InputStream,FileInputStream}
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext

trait TestDataController extends Controller with Utf8MimeTypes {
  protected def retrieve(file: String): Option[InputStream] = {
    AppContext.maybeApp.flatMap { app =>
      if (app.mode == Mode.Prod) {
        // $COVERAGE-OFF$
        app.resourceAsStream(file)
        // $COVERAGE-ON$
      } else {
        app.getExistingFile(file).map(new FileInputStream(_))
      }
    }
  }

  def serve(req: String) = Action.async { implicit request =>
    // $COVERAGE-OFF$
    Logger.debug(s"Request was received for path ${req}")
    // $COVERAGE-ON$
    val path = URLDecoder.decode(req, "UTF-8")

    if (path.startsWith("authorise/read")) {
      Future.successful(Ok(""))
    } else {
      val prefix = "public/sandbox-data"
      val filename = s"${prefix}/${path}.json"
      // $COVERAGE-OFF$
      Logger.debug(s"Looking for file ${filename}")
      // $COVERAGE-ON$
      val data = retrieve(filename).flatMap { is =>
        // $COVERAGE-OFF$
        Logger.debug(s"Found file ${filename} trying to read")
        // $COVERAGE-ON$
        val jsonStr = Source.fromInputStream(is).getLines().mkString("\n")
        // $COVERAGE-OFF$
        Logger.debug(s"Parsing to json")
        // $COVERAGE-ON$
        Some(Json.parse(jsonStr))
      }
      data match {
        case Some(json) => {
          val result = new Status((json \ "status").as[Int])
          (json \ "jsonBody").toOption match {
            case Some(jsonOutStr) => Future.successful(result(jsonOutStr))
            case _ => Future.successful(result("{}"))
          }
        }
        case None => Future.successful(NotFound(s"""{"reason": "Received request ${req} but no file found at '${filename}'"}"""))
      }
    }
  }
}

object SandboxTestDataController extends TestDataController {
}
