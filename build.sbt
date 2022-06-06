import sbt.internal.util.ConsoleAppender
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

import scala.sys.process.ProcessLogger

val appName: String = "apprenticeship-levy"

val XsltConfig = config("api-docs")
val generateAPIDocs = TaskKey[Unit]("api-docs", "Generates HMRC API Documentation files")
generateAPIDocs / managedClasspath := {
  // these are the types of artifacts to include
  val artifactTypes: Set[String] = (generateAPIDocs / classpathTypes).value
  Classpaths.managedJars(XsltConfig, artifactTypes, update.value)
}
val generateAPIDocsTask = generateAPIDocs := {
  val artifactTypes = Set("jar")
  val cp: Seq[java.io.File] = Classpaths.managedJars(XsltConfig, artifactTypes, update.value).map(_.data)
  val log = ConsoleLogger(
    out = ConsoleOut.systemOut,
    ansiCodesSupported = true,
    useFormat = true,
    suppressedMessage = ConsoleAppender.noSuppressedMessage
  )
  val logger = new ProcessLogger() {
    override def buffer[T](f: => T): T = {
      f
    }

    override def out(s: => String): Unit = {}

    override def err(s: => String): Unit = {
      log.error(s)
    }
  }
  val userDir = new File(System.getProperty("user.dir"))
  DocGeneration.generateAPIDocs(userDir, cp) ! logger
}

lazy val AcceptanceTest = config("ac") extend Test

lazy val plugins: Seq[Plugins] = Seq(
  play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory
)

lazy val playSettings: Seq[Setting[_]] = Seq(routesImport ++= Seq(
  "uk.gov.hmrc.apprenticeshiplevy.config.QueryBinders._",
  "org.joda.time.LocalDate",
  "uk.gov.hmrc.apprenticeshiplevy.config.PathBinders._",
  "uk.gov.hmrc.apprenticeshiplevy.data.api.EmploymentReference",
  "uk.gov.hmrc.apprenticeshiplevy.data.api.Nino")
)

lazy val scoverageSettings = {
  val ScoverageExclusionPatterns = List(
    "<empty>",
    "Reverse.*",
    ".*.Routes.*",
    "views.*",
    "prod.*",
    ".*assets.*",
    "uk.gov.hmrc.apprenticeshiplevy.metrics.*",
    "uk.gov.hmrc.apprenticeshiplevy.config.*",
    "testOnlyDoNotUseInAppConf.*",
    "uk.gov.hmrc.BuildInfo"
  )
  Seq(
    ScoverageKeys.coverageExcludedPackages := ScoverageExclusionPatterns.mkString("", ";", ""),
    ScoverageKeys.coverageMinimum := 42.48,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
    playSettings,
    scoverageSettings,
    scalaSettings,
    publishingSettings,
    defaultSettings(),
    scalaVersion := "2.12.12",
    PlayKeys.playDefaultPort := 9470,
    majorVersion := 3,
    ivyConfigurations += XsltConfig,
    libraryDependencies ++= AppDependencies.all,
    libraryDependencies ++= AppDependencies.generateApiTask,
    Test / parallelExecution := false,
    retrieveManaged := true,
    generateAPIDocsTask,
    resolvers += Resolver.jcenterRepo,
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-deprecation",
      "-feature",
      "-P:silencer:pathFilters=routes",
      "-P:silencer:pathFilters=target/.*",
      "-P:silencer:pathFilters=app/uk/gov/hmrc/apprenticeshiplevy/controllers/auth/AuthAction.scala"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    IntegrationTest / unmanagedResourceDirectories += baseDirectory(_ / "public").value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false)
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(
    AcceptanceTest / unmanagedSourceDirectories := (AcceptanceTest / baseDirectory) (base => Seq(base / "ac")).value,
    AcceptanceTest / unmanagedResourceDirectories += baseDirectory(_ / "public").value,
    addTestReportOption(AcceptanceTest, "ac-test-reports"))
  .settings(Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))
