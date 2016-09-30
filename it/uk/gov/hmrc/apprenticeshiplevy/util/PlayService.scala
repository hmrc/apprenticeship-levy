package uk.gov.hmrc.apprenticeshiplevy.util

import play.api.Play
import play.api.test.FakeApplication

trait PlayService extends IntegrationTestConfig {
  lazy val fakeApplication = FakeApplication(additionalConfiguration = additionalConfiguration)

  def start() = {
    println(s"""Starting play with additional configuraion: ${additionalConfiguration.mkString(", ")}""")
    Play.start(fakeApplication)
  }

  def stop() = {
    Play.stop()
    println("Stopped play")
  }
}

object PlayService extends PlayService