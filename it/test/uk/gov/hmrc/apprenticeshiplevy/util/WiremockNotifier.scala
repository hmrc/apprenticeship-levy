/*
 * Copyright 2024 HM Revenue & Customs
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

package test.uk.gov.hmrc.apprenticeshiplevy.util

import java.text.SimpleDateFormat
import java.util.Date

import com.github.tomakehurst.wiremock.common._
import org.scalactic.source
import org.scalatest.{Informer, Informing}
import play.api.Logging

trait WiremockNotifier extends Notifier with Logging {
  protected def verboseWiremockOutput: Boolean
  protected def handleMessage(msg: String): Unit

  private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")

  override def info(message: String): Unit = {
    if (verboseWiremockOutput) processMessage(message)
  }

  override def error(message: String): Unit = {
    processMessage(message, true)
  }

  override def error(message: String, t: Throwable): Unit = {
    processMessage(s"${t.getMessage} on call:")
    processMessage(message)
    t.printStackTrace()
  }

  protected def processMessage(message: String, isError: Boolean = false): Unit = {
    handleMessage(if (isError) s"Wiremock Error: ${df.format(new Date())} ${message}" else s"Wiremock: ${df.format(new Date())} ${message}")
  }
}

class WiremockTestInformerNotifier(var testInformer: Informer, var verboseWiremockOutput: Boolean = true) extends WiremockNotifier {
  protected def handleMessage(msg: String): Unit = {
    testInformer(msg)
  }
}

trait StandardOutInformer extends Informing {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None)(implicit pos:source.Position): Unit = { println(message) }
  }
}

object StandardOutInformer extends StandardOutInformer

trait NullInformer {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None)(implicit pos:source.Position): Unit = { }
  }
}

object NullInformer extends NullInformer

trait LoggerInformer extends Logging {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None)(implicit pos:source.Position): Unit = {
      if (message.contains("ERROR"))
        logger.error(message)
      else
        logger.info(message)
    }
  }
}

object LoggerInformer extends LoggerInformer