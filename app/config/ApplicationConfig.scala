package config

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

object ApplicationConfig extends ApplicationConfig with ServicesConfig {
  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing key: $key"))

  override lazy val etmpUrl: String = loadConfig("apprenticeshipLevy.etmpUrl")
}

trait ApplicationConfig {
  def etmpUrl: String
}