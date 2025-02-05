import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
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
    ".*\\$anon.*"
  )

  // case classes with no added functionality so no requirement to test
  // other than default Reads, Writes or Format
  private val implicitOFormatObjects: Seq[String] = Seq(
    "uk.gov.hmrc.apprenticeshiplevy.utils.*",
    "uk.gov.hmrc.apprenticeshiplevy.data.api.*",
    ".*ApprenticeshipLevy.*",
    ".*DesignatoryDetails.*",
    ".*EmployerPaymentsError.*",
    ".*EmployerPaymentsSummaryVersion0.*",
    ".*EmploymentCheckStatus.*",
    ".*EmptyEmployerPayments.*",
    ".*FractionCalculationDate.*",
    ".*Fractions.*",
    ".*HodAddress.*",
    ".*HodContact.*",
    ".*HodEmail.*",
    ".*HodName.*",
    ".*HodTelephone.*",
    ".*QuestionsAndDeclaration*",
    ".*Fraction*",
    ".*FractionCalculation*"
  )

  val settings: Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageExcludedPackages := (excludedPackages ++ implicitOFormatObjects).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
