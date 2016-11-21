import play.sbt.PlayImport.PlayKeys
import play.sbt.PlayImport.PlayKeys._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._


trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import TestPhases._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import play.sbt.routes.RoutesCompiler.autoImport._
  import play.sbt.routes.RoutesKeys.routesGenerator

  val appName: String

  val defaultPort : Int

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
  lazy val compileScalastyleTask = org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("")
  lazy val playSettings : Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.apprenticeshiplevy.config.QueryBinders._", "org.joda.time.LocalDate",
                                                                     "uk.gov.hmrc.apprenticeshiplevy.config.PathBinders._", "uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference","uk.gov.hmrc.apprenticeshiplevy.data.api.Nino"))
                                                //compile in Compile <<= (compile in Compile) dependsOn compileScalastyleTask)
  lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      ScoverageKeys.coverageExcludedPackages :=  "<empty>;Reverse.*;sandbox.Routes.*;app.Routes.*;views.*;prod.*;.*assets.*;testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.BuildInfo",
      ScoverageKeys.coverageMinimum := 90,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
  }
  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala) ++ plugins : _*)
    .settings(playSettings ++ scoverageSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(PlayKeys.playDefaultPort := defaultPort)
    .settings(
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.8",
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      fork in IntegrationTest := false,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := StaticRoutesGenerator
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      unmanagedResourceDirectories in IntegrationTest <+= baseDirectory (_ / "public"),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      parallelExecution in IntegrationTest := false)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"))
    .settings(
       resolvers += Resolver.bintrayRepo("hmrc", "releases"),
       resolvers += Resolver.jcenterRepo
     )
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
