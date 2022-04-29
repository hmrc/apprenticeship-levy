import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "5.24.0",
    "uk.gov.hmrc"       %% "play-hmrc-api"             % "6.4.0-play-28",
    "uk.gov.hmrc"       %% "play-hal"                  % "3.1.0-play-28",
    "uk.gov.hmrc"       %% "domain"                    % "6.2.0-play-28",
    "uk.gov.hmrc"       %% "time"                      % "3.18.0",
    "com.typesafe.play" %% "play-json-joda"            % "2.9.2"
  )

  lazy val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                    % "3.0.8",
    "com.typesafe.play"      %% "play-test"                    % PlayVersion.current,
    "org.pegdown"             % "pegdown"                      % "1.6.0",
    "org.scalamock"          %% "scalamock-scalatest-support"  % "3.6.0",
    "org.mockito"            %  "mockito-core"                 % "3.6.0",
    "org.scalatestplus.play" %% "scalatestplus-play"           % "5.1.0",
    "com.github.tomakehurst" %  "wiremock-jre8"                % "2.27.2"
  ).map(_ % "test")

  lazy val integrationTest: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"          % "3.1.1",
    "org.pegdown"              % "pegdown"            % "1.6.0",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current,
    "com.github.tomakehurst"   % "wiremock-jre8"      % "2.26.1",
    "org.scala-lang.modules"  %% "scala-xml"          % "1.2.0",
    "com.github.andyglow"     %% "scala-xml-diff"     % "2.0.4",
    "org.scalatestplus"       %% "scalacheck-1-14"    % "3.1.1.1",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0"
  ).map(_ % "test, it")

  lazy val acceptanceTest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest"    % "3.0.8",
    "org.pegdown"    % "pegdown"      % "1.6.0",
    "org.scalaj"    %% "scalaj-http"  % "2.4.2"
  ).map(_ % "test, ac")

  val generateApiTask: Seq[ModuleID] = {
    val XsltConfig = config("api-docs")
    Seq("net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name)
  }

  private val silencerDependencies: Seq[ModuleID] = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.0" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.0" % Provided cross CrossVersion.full
  )

  val all: Seq[ModuleID] = compile ++ test ++ integrationTest ++ acceptanceTest ++ silencerDependencies
}