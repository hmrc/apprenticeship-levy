package uk.gov.hmrc.apprenticeshiplevy.config

object Config {

  lazy val rootUrl = {
    val environmentProperty = System.getProperty("environment", "local").toLowerCase
    environmentProperty match {
      case "local" => "http://localhost:9470"
      case "dev" => throw new IllegalArgumentException(s"Provide dev endpoint")
      case "qa" => throw new IllegalArgumentException(s"Provide qa endpoint")
      case "staging" => throw new IllegalArgumentException(s"Provide staging endpoint")
      case _ => throw new IllegalArgumentException(s"Environment '$environmentProperty' not known")
    }
  }

  lazy val resourceDir = {
    val environmentProperty = System.getProperty("environment", "local").toLowerCase
    environmentProperty match {
      case "local" => s"""${System.getProperty("user.dir")}/ac/resources/local"""
      case "dev" => s"""${System.getProperty("user.dir")}/ac/resources/dev"""
      case "qa" => s"""${System.getProperty("user.dir")}/ac/resources/qa"""
      case "staging" => s"""${System.getProperty("user.dir")}/ac/resources/staging"""
      case _ => throw new IllegalArgumentException(s"Environment '$environmentProperty' not known")
    }
  }

  lazy val contexts = {
    val environmentProperty = System.getProperty("environment", "local").toLowerCase
    environmentProperty match {
      case "local" => Seq(("/sandbox","sandbox"),("","live"))
      case "dev" => Seq(("","sandbox"),("","live"))
      case "qa" => Seq(("","sandbox"),("","live"))
      case "staging" => Seq(("","sandbox"),("","live"))
      case _ => throw new IllegalArgumentException(s"Environment '$environmentProperty' not known")
    }
  }

  lazy val isLocalEnvironment = System.getProperty("environment", "local").toLowerCase == "local"
}