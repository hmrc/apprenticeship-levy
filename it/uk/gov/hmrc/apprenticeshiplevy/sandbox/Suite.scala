package uk.gov.hmrc.apprenticeshiplevy.sandbox

import org.scalatest._

@DoNotDiscover
class Suite extends Suites(new SandboxRootControllerISpec,
                           new SandboxEmprefControllerISpec,
                           new SandboxEmploymentCheckControllerISpec,
                           new SandboxFractionsCalculationControllerISpec)