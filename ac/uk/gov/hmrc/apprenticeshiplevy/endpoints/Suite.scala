package uk.gov.hmrc.apprenticeshiplevy.endpoints

import org.scalatest._

class EndpointSuite extends Suites(new RootSpec,
                                   new EmprefSpec,
                                   new FractionsSpec,
                                   new LevyDeclarationsSpec,
                                   new EmploymentCheckSpec) {
}