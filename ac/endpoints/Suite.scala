package endpoints

import org.scalatest.*

class EndpointSuite extends Suites(new RootSpec,
                                   new EmprefSpec,
                                   new FractionsSpec,
                                   new LevyDeclarationsSpec,
                                   new EmploymentCheckSpec) {
}