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
import org.joda.time._
import uk.gov.hmrc.time.DateConverter
import scala.util.{Try, Success, Failure}
import org.slf4j.MDC

trait TestDataController extends Controller with Utf8MimeTypes {
  val SANDBOX_DATA_DIR = "public/sandbox-data"

  protected def retrieve(file: String): Option[InputStream] = {
    AppContext.maybeApp.flatMap { app =>
      if (file.startsWith(SANDBOX_DATA_DIR)) {
        if (app.mode == Mode.Prod) {
          // $COVERAGE-OFF$
          Logger.debug(s"Getting resource stream ${file}")
          app.resourceAsStream(file)
          // $COVERAGE-ON$
        } else {
          // $COVERAGE-OFF$
          Logger.debug(s"Getting file input stream ${file}")
          // $COVERAGE-ON$
          app.getExistingFile(file).map(new FileInputStream(_))
        }
      } else {
          // $COVERAGE-OFF$
          Logger.debug(s"Getting file input stream ${file}")
          // $COVERAGE-ON$
          Try(new FileInputStream(new File(file))).toOption
      }
    }
  }

  def serve(req: String) = Action.async { implicit request =>
    MDC.put("X-Client-ID",request.headers.toSimpleMap.getOrElse("X-Client-ID","Unknown caller"))

    // $COVERAGE-OFF$
    Logger.debug(s"Request was received for path ${req}")
    // $COVERAGE-ON$
    val path = URLDecoder.decode(req, "UTF-8")
    val Empref = "(.*/)([a-zA-Z0-9]{3}/[a-zA-Z0-9]+)(/.*)".r
    request.headers.get("OVERRIDE_EMPREF") match {
      case Some(empref) => {
        path match {
          case Empref(path1, old_empref, path2) => {
            val newpath = s"${path1}${empref}${path2}"
            Logger.info(s"Resource path overridden by OVERRIDE_EMPREF header. Now looking for ${newpath}")
            readJson(SANDBOX_DATA_DIR, newpath)
            .orElse(readJson(System.getProperty("extra.sandbox-data.dir",""), newpath))
            .getOrElse(Future.successful(NotFound(s"""{"reason": "Received request ${req} with OVERRIDE_EMPREF header of ${empref} but no file '${newpath}' found in '${SANDBOX_DATA_DIR}' or '${System.getProperty("extra.sandbox-data.dir","")}'"}""")))
          }
          case _ => {
            Logger.warn(s"Resource path overridden by OVERRIDE_EMPREF header but unable to get new empref from ${path}.")
            Future.successful(NotFound(s"""{"reason": "Received request ${req} but no file '${path}' found in '${SANDBOX_DATA_DIR}' or '${System.getProperty("extra.sandbox-data.dir","")}'"}"""))
          }
        }
      }
      case _ => {
        if (path.startsWith("authorise/read")) {
          Future.successful(Ok(""))
        } else {
          Logger.info(s"""No override header found. Headers: ${request.headers.toSimpleMap.mkString(" ")}""")
          readJson(SANDBOX_DATA_DIR, path)
            .orElse(readJson(System.getProperty("extra.sandbox-data.dir",""), path))
            .getOrElse(Future.successful(NotFound(s"""{"reason": "Received request ${req} but no file '${path}' found in '${SANDBOX_DATA_DIR}' or '${System.getProperty("extra.sandbox-data.dir","")}'"}""")))
        }
      }
    }
  }

  protected def readJson(dir: String, path: String)(implicit request: Request[_]): Option[Future[Result]] = {
    val filename = s"${dir}/${path}.json"
    // $COVERAGE-OFF$
    Logger.debug(s"Looking for file ${filename}")
    // $COVERAGE-ON$
    getData(filename).map(json=>filterByDate(path, json))
  }

  protected def getData(filename: String): Option[JsValue] = retrieve(filename).flatMap { is =>
    // $COVERAGE-OFF$
    Logger.debug(s"Found file ${filename} trying to read")
    // $COVERAGE-ON$
    val jsonStr = Source.fromInputStream(is).getLines().mkString("\n")
    // $COVERAGE-OFF$
    Logger.debug(s"Parsing to json")
    // $COVERAGE-ON$
    Some(Json.parse(jsonStr))
  }

  protected def toInstant(dateStr: String): Instant = {
    val time = LocalTime.MIDNIGHT
    val zone = DateTimeZone.getDefault()
    LocalDate.parse(dateStr).toDateTime(time, zone).toInstant()
  }

  protected def toInstant(dateStr: String, days: Int): Instant = {
    val time = LocalTime.MIDNIGHT
    val zone = DateTimeZone.getDefault()
    LocalDate.parse(dateStr).plusDays(days).toDateTime(time, zone).toInstant()
  }

  protected def toInstant(json: JsLookupResult): Instant = {
    val time = LocalTime.MIDNIGHT
    val zone = DateTimeZone.getDefault()
    Try(json.as[LocalDate].toDateTime(time, zone).toInstant()).recover{
      case e: Throwable => LocalDateTime.parse(json.as[String]).toDateTime().toInstant()
    }.get
  }

  protected def toInstant(json: JsLookupResult, days: Int): Instant = {
    val time = LocalTime.MIDNIGHT
    val zone = DateTimeZone.getDefault()
    Try(json.as[LocalDate].toDateTime(time, zone).toInstant()).recover{
      case e: Throwable => LocalDateTime.parse(json.as[String]).plusDays(days).toDateTime().toInstant()
    }.get
  }

  protected def filterByDate(path: String, json: JsValue)(implicit request: Request[_]): Future[Result] = {
    val result = new Status((json \ "status").as[Int])

    (json \ "jsonBody").toOption match {
      case Some(jsonOutStr) => {
        (request.getQueryString("fromDate"), request.getQueryString("toDate")) match {
          case (maybeFrom,maybeTo) if maybeFrom.isDefined && maybeTo.isDefined  => {
            Logger.debug(s"Filtering results to: ${maybeFrom} ${maybeTo}")
            val queryInterval = new Interval(maybeFrom.map(toInstant(_)).get,
                                             maybeTo.map(toInstant(_, 1)).get)
            val EmployedCheck = "([A-Za-z\\-/0-9]*)(employed)([A-Za-z\\-/0-9]*)".r
            val Fractions = "([A-Za-z\\-/0-9%]*)(fractions)".r
            val Declarations = "([A-Za-z\\-/0-9%]*)(employer-payment-summary)".r
            path match {
              case EmployedCheck(_,_,_) => {
                val interval = new Interval(toInstant(json \ "jsonBody" \ "fromDate"),
                                            toInstant(json \ "jsonBody" \ "toDate", 1))
                if (interval.overlap(queryInterval) != null) {
                  Future.successful(result(jsonOutStr))
                } else {
                  Future.successful(result(((json \ "jsonBody").as[JsObject] - "employed") + ("employed" -> Json.toJson(false))))
                }
              }
              case Fractions(_,_) => {
                val arr = (json \ "jsonBody" \ "fractionCalculations").as[JsArray].value.filter(v => queryInterval.contains(toInstant(v \ "calculatedAt")))
                Future.successful(result(((json \ "jsonBody").as[JsObject] - "fractionCalculations") + ("fractionCalculations" -> new JsArray(arr))))
              }
              case Declarations(_,_) => {
                val arr = (json \ "jsonBody" \ "eps").as[JsArray].value.filter(v => queryInterval.contains(toInstant(v \ "hmrcSubmissionTime")))
                Future.successful(result(((json \ "jsonBody").as[JsObject] - "eps") + ("eps" -> new JsArray(arr))))
              }
              case _ => Future.successful(result(jsonOutStr))
            }
          }
          case (_,_) => Future.successful(result(jsonOutStr))
        }
      }
      case _ => Future.successful(result("{}"))
    }
  }
}

object SandboxTestDataController extends TestDataController {
}
