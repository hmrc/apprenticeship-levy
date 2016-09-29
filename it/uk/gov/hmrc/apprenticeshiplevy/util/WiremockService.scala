package uk.gov.hmrc.apprenticeshiplevy.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.Informer
import com.github.tomakehurst.wiremock.common._

trait StandardOutInformer {
  lazy val info = new Informer {
    def apply(message: String, payload: Option[Any] = None): Unit = { println(message) }
  }
}

trait WiremockService extends IntegrationTestConfig with StandardOutInformer {
  info(s"Configuring wire mock server to listen on ${stubHost}:${stubPort} using responses configured in ${stubConfigPath}")
  lazy val wireMockServer = new WireMockServer(wireMockConfig.notifier(WiremockTestInformerNotifier(info, verboseWiremockOutput)).usingFilesUnderDirectory(stubConfigPath).port(stubPort).bindAddress(stubHost))

  def start() = {
    info(s"Starting wirework....")
    wireMockServer.start()
  }

  def stop() = {
    info(s"Stopping wirework....")
    wireMockServer.stop()
  }
}

object WiremockService extends WiremockService