import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "apprenticeship-levy"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override val defaultPort: Int = 9470
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % "10.5.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "3.4.0-play-25",
    "uk.gov.hmrc" %% "play-hal" % "1.8.0-play-25",
    "uk.gov.hmrc" %% "domain" % "5.3.0",
    "uk.gov.hmrc" %% "play-authorised-frontend" % "7.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    def test: Seq[ModuleID]
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.5.0-play-25" % scope,
        "org.scalatest" %% "scalatest" % "3.0.5" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
        "org.mockito" % "mockito-all" % "1.10.19" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.5.0-play-25" % scope,
        "org.scalatest" %% "scalatest" % "3.0.5" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % "2.21.0" % scope,
        "org.scala-lang.modules" %% "scala-xml" % "1.1.1" % scope,
        "com.github.andyglow" %% "scala-xml-diff" % "2.0.3" % scope,
        "org.scalacheck" %% "scalacheck" % "1.14.0" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
      )
    }.test
  }

  object AcceptanceTest {
    def apply() = new TestDependencies {
      override lazy val scope: String = "ac"
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.5.0-play-25" % scope,
        "org.scalatest" %% "scalatest" % "3.0.5" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.scalaj" %% "scalaj-http" % "2.4.1"
      )
    }.test
  }
  def apply() = compile ++ Test() ++ IntegrationTest() ++ AcceptanceTest()
}
