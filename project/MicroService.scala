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

  val XsltConfig = config("api-docs")
  val generateAPIDocs = TaskKey[Unit]("api-docs", "Generates HMRC API Documentation files")
  managedClasspath in generateAPIDocs := {
      // these are the types of artifacts to include
      val artifactTypes: Set[String] = (classpathTypes in generateAPIDocs).value
      Classpaths.managedJars(XsltConfig, artifactTypes, update.value)
  }
  val generateAPIDocsTask = generateAPIDocs := {
    val artifactTypes = Set("jar")
    val cp: Seq[java.io.File] = Classpaths.managedJars(XsltConfig, artifactTypes, update.value).map(_.data)
    val log = ConsoleLogger(ConsoleOut.systemOut, true, true, ConsoleLogger.noSuppressedMessage)
    val logger = new ProcessLogger() {
      def buffer[T](f: => T): T = { f }
      def error(s: => String): Unit = { log.error(s) }
      def info(s: => String): Unit = {  }
    }
    val userDir = new File(System.getProperty("user.dir"))
    DocGeneration.generateAPIDocs(userDir, cp) ! logger
  }

  lazy val AcceptanceTest = config("ac") extend(Test)
  lazy val appDependencies : Seq[ModuleID] = Seq.empty
  lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
  lazy val compileScalastyleTask = org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("")
  lazy val playSettings : Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.apprenticeshiplevy.config.QueryBinders._", "org.joda.time.LocalDate",
                                                                     "uk.gov.hmrc.apprenticeshiplevy.config.PathBinders._", "uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference","uk.gov.hmrc.apprenticeshiplevy.data.api.Nino"))
  lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      ScoverageKeys.coverageExcludedPackages :=  "<empty>;Reverse.*;sandbox.Routes.*;app.Routes.*;views.*;prod.*;.*assets.*;testOnlyDoNotUseInAppConf.*;uk.gov.hmrc.BuildInfo",
      ScoverageKeys.coverageMinimum := 85,
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
      ivyConfigurations += XsltConfig,
      libraryDependencies ++= appDependencies,
      libraryDependencies += "net.sourceforge.saxon" % "saxon" % "9.1.0.8" % XsltConfig.name,
      parallelExecution in Test := false,
      fork in Test := false,
      fork in IntegrationTest := false,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      generateAPIDocsTask,
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
    .configs(AcceptanceTest)
    .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
    .settings(
      Keys.fork in AcceptanceTest := false,
      unmanagedSourceDirectories in AcceptanceTest <<= (baseDirectory in AcceptanceTest)(base => Seq(base / "ac")),
      unmanagedResourceDirectories in AcceptanceTest <+= baseDirectory (_ / "public"),
      addTestReportOption(AcceptanceTest, "ac-test-reports"),
      parallelExecution in AcceptanceTest := false)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"))
    .settings(
       resolvers += Resolver.bintrayRepo("hmrc", "releases"),
       resolvers += Resolver.jcenterRepo
     )
    .settings(testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
