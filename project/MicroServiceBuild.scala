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

  private val microserviceBootstrapVersion = "5.8.0"
  private val playAuthVersion = "4.2.0"
  private val playAuthFrontendVersion = "6.2.0"
  private val playHealthVersion = "2.0.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val playUrlBindersVersion = "2.0.0"
  private val playConfigVersion = "3.0.0"
  private val playHmrcApiVersion = "1.2.0"
  private val hmrcTestVersion = "2.1.0"
  private val playHalVersion = "1.1.0"
  private val scalaXMLVersion = "2.11.0-M4"
  private val xmlDiffVersion = "2.0.2"
  private val scalacheckVersion = "1.12.5"
  private val playAuditingVersion = "2.4.0"
  private val domainVersion = "4.0.0"
  private val httpVerbsVersion= "3.3.0"
  private val scalajHttpVersion = "2.3.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthFrontendVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % playHmrcApiVersion,
    "uk.gov.hmrc" %% "play-hal" % playHalVersion,
    "uk.gov.hmrc" %% "play-auditing" % playAuditingVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "com.kenshoo" %% "metrics-play" % "2.4.0_0.4.1"
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
        "org.pegdown" % "pegdown" % "1.5.0" % scope,
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
        "org.scalaj" %% "scalaj-http" % scalajHttpVersion,
        "uk.gov.hmrc" %% "http-verbs" % httpVerbsVersion % scope
      )
    }.test
  }
  def apply() = compile ++ Test() ++ IntegrationTest() ++ AcceptanceTest()
}

