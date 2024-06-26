import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.0.0"
  val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %%  s"play-hal-$playVersion"         % "4.0.0",
    "uk.gov.hmrc" %% s"domain-$playVersion"            % "9.0.0"

  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"      % bootstrapVersion,
    "org.playframework"      %% "play-test"                         % "3.0.4",
    "org.pegdown"             % "pegdown"                           % "1.6.0",
    "org.mockito"             % "mockito-core"                      % "5.11.0"
  ).map(_ % "test")

  lazy val integrationTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                      % "1.6.0",
    "org.playframework"      %% "play-test"                    % "3.0.4",
    "org.scala-lang.modules" %% "scala-xml"                    % "2.2.0",
    "com.github.andyglow"    %% "scala-xml-diff"               % "3.0.1",
    "org.scalatestplus"      %% "scalacheck-1-14"              % "3.2.2.0"
  ).map(_ % "test")

  lazy val acceptanceTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.pegdown"    % "pegdown"                      % "1.6.0",
    "org.scalaj"    %% "scalaj-http"                  % "2.4.2"
  ).map(_ % "test, ac")

  val generateApiTask: Seq[ModuleID] = {
    val XsltConfig = config("api-docs")

    Seq(
      "net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name
    )
  }

  val all: Seq[ModuleID] = compile ++ test ++ integrationTest ++ acceptanceTest
}
