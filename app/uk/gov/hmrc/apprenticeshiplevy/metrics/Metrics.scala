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

package uk.gov.hmrc.apprenticeshiplevy.metrics

import java.util.concurrent.TimeUnit

import com.codahale.metrics._
import com.kenshoo.play.metrics.MetricsImpl
import play.api.Logger
import play.api.Play.current
import uk.gov.hmrc.apprenticeshiplevy.config.AppContext

import scala.util.Try

sealed trait MetricEvent {
  def metric(): String
  def name(): String
}

case class RequestEvent(name: String, maybeEmpref: Option[String]) extends MetricEvent {
  def metric(): String = s"""${name}${maybeEmpref.map("." + _).getOrElse("")}"""
}

case class TimerEvent(name: String, delta: Long, timeUnit: TimeUnit) extends MetricEvent {
  def metric(): String = name
}

trait GraphiteMetrics {
  Logger.info("[Metrics] Registering metrics...")

  val registry: Option[MetricRegistry] = if (AppContext.metricsEnabled) Try (Some(current.injector.instanceOf[MetricsImpl].defaultRegistry)).getOrElse(None) else None

  val AUTH_SERVICE_REQUEST = "auth-service"
  val DES_EMP_CHECK_REQUEST = "des-emp-check"
  val DES_EMPREF_DETAILS_REQUEST = "des-emp-details"
  val DES_FRACTIONS_REQUEST = "des-fractions"
  val DES_FRACTIONS_DATE_REQUEST = "des-fractions"
  val DES_LEVIES_REQUEST = "des-levies"

  private val timer = (name: String) => registry.map(_.timer(name))
  private val meter = (name: String) => registry.map(_.meter(name))

  private val log = (name: String, delta: Long, timeUnit: TimeUnit) => {
    timer(name) match {
      case Some(tmr) => {
        // $COVERAGE-OFF$
        Logger.trace(s"[Metrics][${name}] ${delta} ${timeUnit}")
        // $COVERAGE-ON$
        tmr.update(delta, timeUnit)
      }
      case _ => Logger.trace(s"[Metrics][${name}] Not enabled")
    }
  }

  private val mark = (name: String) => {
    meter(name) match {
      case Some(mtr) => {
        // $COVERAGE-OFF$
        Logger.trace(s"[Metrics][${name}] ${mtr.getCount()}")
        // $COVERAGE-ON$
        mtr.mark
      }
      case _ =>
        // $COVERAGE-OFF$
        Logger.trace(s"[Metrics][${name}] Not enabled")
        // $COVERAGE-ON$
    }
  }

  def successfulRequest(event: MetricEvent): Unit = event match {
    case RequestEvent(name, Some(_)) => mark(s"ala.success.${name}")
    case _ => mark(s"ala.success.${event.name}")
  }

  def failedRequest(event: MetricEvent): Unit = event match {
    case RequestEvent(name, Some(_)) => mark(s"ala.failed.${name}")
    case _ => mark(s"ala.failed.${event.name}")
  }

  def processRequest(event: TimerEvent): Unit = log(s"ala.timers.${event.name}", event.delta, event.timeUnit)

  registry match {
    case Some(_) =>
      // $COVERAGE-OFF$
      Logger.info("[Metrics] Completed metrics registration.")
      // $COVERAGE-ON$
    case None =>
      // $COVERAGE-OFF$
      Logger.warn("[Metrics] Metrics disabled either 'microservice.metrics.graphite.enabled' has been set to false or no Play Application started.")
      // $COVERAGE-ON$
  }
}
