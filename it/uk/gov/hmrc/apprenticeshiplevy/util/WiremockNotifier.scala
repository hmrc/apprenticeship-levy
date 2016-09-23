package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.common._
import org.scalatest.Informer

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

trait WiremockNotifier extends Notifier {
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

case class WiremockTestInformerNotifier(testInformer: Informer, verboseWiremockOutput: Boolean = true) extends WiremockNotifier {
  protected def handleMessage(msg: String): Unit = testInformer(msg)
}