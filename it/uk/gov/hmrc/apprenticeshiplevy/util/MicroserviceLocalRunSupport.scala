package uk.gov.hmrc.apprenticeshiplevy.util

import play.api.Play
import play.api.test.FakeApplication

trait MicroserviceLocalRunSupport {
  val additionalConfiguration: Map[String, Any]
  val port = sys.env.getOrElse("MICROSERVICE_PORT", "9001").toInt
  val localMicroserviceUrl = s"http://localhost:$port"
  lazy val fakeApplication = FakeApplication(additionalConfiguration = additionalConfiguration)

  def run(block: () => Unit) = {
    Play.start(fakeApplication)
    block()
    Play.stop()
  }
}