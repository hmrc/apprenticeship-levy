import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "6.3.0",
    "uk.gov.hmrc" %% "play-hmrc-api"             % "7.0.0-play-28",
    "uk.gov.hmrc" %% "play-hal"                  % "3.1.0-play-28",
    "uk.gov.hmrc" %% "domain"                    % "8.1.0-play-28",
    "uk.gov.hmrc" %% "time"                      % "3.25.0"
  )

  lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"      % "6.3.0",
    "com.typesafe.play"      %% "play-test"                   % PlayVersion.current,
    "org.pegdown"             % "pegdown"                     % "1.6.0",
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.6.0",
    "org.mockito"            %  "mockito-core"                % "4.6.1",
    "com.github.tomakehurst" %  "wiremock-jre8"               % "2.27.2"
  ).map(_ % "test")

  lazy val integrationTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.24.0",
    "org.pegdown"             % "pegdown"                % "1.6.0",
    "com.typesafe.play"      %% "play-test"              % PlayVersion.current,
    "com.github.tomakehurst"  % "wiremock-jre8"          % "2.27.2",
    "org.scala-lang.modules" %% "scala-xml"              % "2.1.0",
    "com.github.andyglow"    %% "scala-xml-diff"         % "3.0.0",
    "org.scalatestplus"      %% "scalacheck-1-14"        % "3.2.2.0"
  ).map(_ % "test,it")

  lazy val acceptanceTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-28" % "6.3.0",
    "org.pegdown"    % "pegdown"                % "1.6.0",
    "org.scalaj"    %% "scalaj-http"            % "2.4.2",
  ).map(_ % "test, ac")

  val generateApiTask: Seq[ModuleID] = {
    val XsltConfig = config("api-docs")

    Seq(
      "net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name
    )
  }

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.0" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.0" % Provided cross CrossVersion.full
  )

  val all: Seq[ModuleID] = compile ++ test ++ integrationTest ++ acceptanceTest ++ silencerDependencies
}
