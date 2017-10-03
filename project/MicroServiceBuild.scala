import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "apprenticeship-levy"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override val defaultPort: Int = 9470
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "6.9.0"
  private val playConfigVersion = "5.0.0"
  private val playHmrcApiVersion = "1.4.0"
  private val hmrcTestVersion = "2.4.0"
  private val playHalVersion = "1.1.0"
  private val scalaXMLVersion = "2.11.0-M4"
  private val xmlDiffVersion = "2.0.2"
  private val scalacheckVersion = "1.12.5"
  private val playAuditingVersion = "3.2.0"
  private val domainVersion = "4.1.0"
  private val pegdownVersion = "1.6.0"
  private val scalajHttpVersion = "2.3.0"
  private val playAuthVersion = "5.1.0"
  private val playAuthFrontendVersion = "7.0.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % playHmrcApiVersion,
    "uk.gov.hmrc" %% "play-hal" % playHalVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthFrontendVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    def test: Seq[ModuleID]
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % scope,
        "org.mockito" % "mockito-all" % "1.9.5" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % "2.5.1" % scope,
        "org.scala-lang" % "scala-xml" % scalaXMLVersion % scope,
        "com.github.andyglow" % "scala-xml-diff_2.11" % xmlDiffVersion % scope,
        "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope
      )
    }.test
  }

  object AcceptanceTest {
    def apply() = new TestDependencies {
      override lazy val scope: String = "ac"
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "2.2.6" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
        "org.scalaj" %% "scalaj-http" % scalajHttpVersion
      )
    }.test
  }
  def apply() = compile ++ Test() ++ IntegrationTest() ++ AcceptanceTest()
}
