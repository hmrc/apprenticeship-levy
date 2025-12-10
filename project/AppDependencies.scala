
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.4.0"
  val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-hal-$playVersion"          % "4.1.0",
    "uk.gov.hmrc" %% s"domain-$playVersion"            % "13.0.0"
  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.scalatestplus" %% "scalacheck-1-18"              % "3.2.19.0",
    "org.scalaj"        %% "scalaj-http"                  % "2.4.2" cross CrossVersion.for3Use2_13
  ).map(_ % Test)

  val generateApiTask: Seq[ModuleID] = {
    val XsltConfig = config("api-docs")

    Seq(
      "net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name
    )
  }

  val all: Seq[ModuleID] = compile ++ test
}
