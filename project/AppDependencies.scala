import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap"   % "10.6.0",
    "uk.gov.hmrc" %% "play-hmrc-api"            % "3.6.0-play-25",
    "uk.gov.hmrc" %% "play-hal"                 % "1.9.0-play-25",
    "uk.gov.hmrc" %% "domain"                   % "5.6.0-play-25",
    "uk.gov.hmrc" %% "play-authorised-frontend" % "7.1.0"
  )

   lazy val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "hmrctest"                     % "3.9.0-play-25",
    "org.scalatest"          %% "scalatest"                    % "3.0.8",
    "com.typesafe.play"      %% "play-test"                    % PlayVersion.current,
    "org.pegdown"             % "pegdown"                      % "1.6.0",
    "org.scalamock"          %% "scalamock-scalatest-support"  % "3.6.0",
    "org.mockito"             % "mockito-all"                  % "1.10.19",
    "org.scalatestplus.play" %% "scalatestplus-play"           % "2.0.1"
   ).map(_ % "test")

   lazy val integrationTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"           % "3.9.0-play-25",
    "org.scalatest"           %% "scalatest"          % "3.0.8",
    "org.pegdown"              % "pegdown"            % "1.6.0",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current,
    "com.github.tomakehurst"   % "wiremock"           % "2.24.1",
    "org.scala-lang.modules"  %% "scala-xml"          % "1.2.0",
    "com.github.andyglow"     %% "scala-xml-diff"     % "2.0.4",
    "org.scalacheck"          %% "scalacheck"         % "1.14.0",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "2.0.1"
   ).map(_ % "test,it")

   lazy val acceptanceTest: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% "hmrctest"     % "3.9.0-play-25",
    "org.scalatest" %% "scalatest"    % "3.0.8",
    "org.pegdown"    % "pegdown"      % "1.6.0",
    "org.scalaj"    %% "scalaj-http"  % "2.4.2"
   ).map(_ % "test, ac")

  val generateApiTask: Seq[ModuleID] = {
    val XsltConfig = config("api-docs")

    Seq(
      "net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name
    )
  }

  val all: Seq[ModuleID] = compile ++ test ++ integrationTest ++ acceptanceTest
}
