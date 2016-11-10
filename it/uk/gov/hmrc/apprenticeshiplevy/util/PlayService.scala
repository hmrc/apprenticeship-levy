package uk.gov.hmrc.apprenticeshiplevy.util

import play.api.Play
import play.api.test.FakeApplication
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig

trait PlayService extends IntegrationTestConfig {
  lazy val fakeApplication = FakeApplication(additionalConfiguration = additionalConfiguration)

  def start() = {
    Play.start(fakeApplication)
  }

  def stop() = {
    Play.stop()
  }
}

object PlayService extends PlayService