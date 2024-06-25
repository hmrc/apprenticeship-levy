import sbt.internal.util.ConsoleAppender
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.*

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
  play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
)

lazy val playSettings: Seq[Setting[?]] = Seq(routesImport ++= Seq(
  "uk.gov.hmrc.apprenticeshiplevy.config.QueryBinders._",
  "java.time.LocalDate",
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
    "uk.gov.hmrc.apprenticeshiplevy.controllers.live.*",
    "uk.gov.hmrc.apprenticeshiplevy.controllers.sandbox.*",
    "testOnlyDoNotUseInAppConf.*",
    "uk.gov.hmrc.BuildInfo"
  )
  Seq(
    ScoverageKeys.coverageExcludedPackages := ScoverageExclusionPatterns.mkString("", ";", ""),
    ScoverageKeys.coverageMinimumStmtTotal := 41.29,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

ThisBuild / majorVersion := 3
ThisBuild / scalaVersion := "2.13.14"

val microservice = Project(appName, file("."))
  .enablePlugins(plugins *)
  .settings(
    playSettings,
    scoverageSettings,
    scalaSettings,
    defaultSettings(),
    PlayKeys.playDefaultPort := 9470,
    ivyConfigurations += XsltConfig,
    libraryDependencies ++= AppDependencies.all,
    libraryDependencies ++= AppDependencies.generateApiTask,
    Test / parallelExecution := false,
    retrieveManaged := true,
    generateAPIDocsTask,
    resolvers += Resolver.jcenterRepo,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xmaxerrs", "1000", // Maximum errors to print
      "-Xmaxwarns", "1000", // Maximum warnings to print
      "-Wconf:src=routes/.*:is,src=twirl/.*:is"
    )
  )
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings) *)
  .settings(
    AcceptanceTest / unmanagedSourceDirectories := (AcceptanceTest / baseDirectory) (base => Seq(base / "ac")).value,
    AcceptanceTest / unmanagedResourceDirectories += baseDirectory(_ / "public").value,
    addTestReportOption(AcceptanceTest, "ac-test-reports"))
  .settings(Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))

val it: Project = project.in(file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
