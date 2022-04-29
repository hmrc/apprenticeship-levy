package uk.gov.hmrc.apprenticeshiplevy.config

trait IntegrationTestConfig {
  System.setProperty("logger.resource", "logback-test.xml")

  def verboseWiremockOutput: Boolean = sys.props.getOrElse("WIREMOCK_VERBOSE_OUTPUT", "false").toBoolean

  def stubPort: Int = sys.props.getOrElse("WIREMOCK_PORT", "8080").toInt

  def stubHost: String = sys.props.getOrElse("WIREMOCK_HOST", "localhost")

  def stubConfigPath: String = sys.props.getOrElse("WIREMOCK_MAPPINGS", "./it/resources")

  def resourcePath: String = sys.props.getOrElse("RESOURCE_PATH", "./it/resources")

  def test_host: String = sys.props.getOrElse("MICROSERVICE_HOST", "localhost")

  def test_port: Int = sys.props.getOrElse("MICROSERVICE_PORT", "9001").toInt

  def localMicroserviceUrl = s"http://$test_host:$test_port"

  val controllerSettings: Seq[(String, String)] = Seq(
    "LiveLevyDeclarationController",
    "LiveRootController",
    "LiveEmprefController",
    "LiveFractionsController",
    "LiveFractionsCalculationDateController",
    "LiveEmploymentCheckController"
  ).map(
    a =>
      (s"controllers.uk.gov.hmrc.apprenticeshiplevy.controllers.live.$a.needsAuditing", "false")
  )

  def additionalConfiguration: Map[String, Any] = Map(
    "play.ws.timeout.request" -> "500 milliseconds",
    "play.ws.timeout.connection" -> "500 milliseconds",
    "http.port" -> test_port,
    "auditing.enabled" -> "false",
    "microservice.private-mode" -> "true",
    "appName" -> "application-name",
    "appUrl" -> "http://microservice-name",
    "microservice.metrics.graphite.enabled" -> "false",
    "microservice.services.stub-auth.host" -> stubHost,
    "microservice.services.stub-auth.port" -> stubPort,
    "microservice.services.stub-auth.path" -> "",
    "microservice.services.stub-des.host" -> stubHost,
    "microservice.services.stub-des.port" -> stubPort,
    "microservice.services.stub-des.path" -> "",
    "microservice.services.des.host" -> stubHost,
    "microservice.services.des.port" -> stubPort,
    "microservice.services.des.path" -> "",
    "microservice.services.auth.host" -> stubHost,
    "microservice.services.auth.port" -> stubPort,
    "microservice.services.auth.path" -> "",
    "microservice.whitelisted-applications" -> "myappid1,myappid2"
  ) ++ Map(controllerSettings: _*)
}
