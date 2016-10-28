package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.common._
import org.scalatest.Informer

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.api.Logger

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

class WiremockTestInformerNotifier(var testInformer: Informer, var verboseWiremockOutput: Boolean = true) extends WiremockNotifier {
  protected def handleMessage(msg: String): Unit = {
    if (msg.contains("read") && !sys.props.get("play.crypto.secret").isDefined) {
      testInformer("Wiremock Error: Request was not matched. Please set 'play.crypto.secret' system property")
    } else {
      testInformer(msg)
    }
    throw new org.scalatest.exceptions.TestFailedException("Wiremock error", 3)
  }
}

trait StandardOutInformer {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None): Unit = { println(message) }
  }
}

object StandardOutInformer extends StandardOutInformer

trait NullInformer {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None): Unit = { }
  }
}

object NullInformer extends NullInformer

trait LoggerInformer {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None): Unit = {
      if (message.contains("ERROR"))
        Logger("wiremock").error(message)
      else
        Logger("wiremock").info(message)
    }
  }
}

object LoggerInformer extends LoggerInformer