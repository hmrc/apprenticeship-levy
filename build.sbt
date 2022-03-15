import scala.sys.process.ProcessLogger
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, _}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName: String = "apprenticeship-levy"

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

lazy val AcceptanceTest = config("ac") extend (Test)

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
    ScoverageKeys.coverageMinimum := 85,
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
    parallelExecution in Test := false,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    generateAPIDocsTask,
    resolvers += Resolver.jcenterRepo,
    scalacOptions ++= Seq("-P:silencer:pathFilters=routes")
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest) (base => Seq(base / "it")).value,
    unmanagedResourceDirectories in IntegrationTest += baseDirectory(_ / "public").value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    parallelExecution in IntegrationTest := false)
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(
    unmanagedSourceDirectories in AcceptanceTest := (baseDirectory in AcceptanceTest) (base => Seq(base / "ac")).value,
    unmanagedResourceDirectories in AcceptanceTest += baseDirectory(_ / "public").value,
    addTestReportOption(AcceptanceTest, "ac-test-reports"))
  .settings(testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))
