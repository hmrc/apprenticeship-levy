/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apprenticeshiplevy.controllers

import org.joda.time.LocalDate
import org.scalatest.concurrent.ScalaFutures
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.connectors.LiveDesConnector
import uk.gov.hmrc.apprenticeshiplevy.controllers.live.LiveFractionsController
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import uk.gov.hmrc.play.test.UnitSpec

class FractionsControllerSpec extends UnitSpec with ScalaFutures {
  val liveFractionsController = new LiveFractionsController(new LiveDesConnector)

  "getting the fractions" should {
    "return a Not Acceptable response if the Accept header is not correctly set" in {
      val response = liveFractionsController.fractions(EmploymentReference("empref"), None, None)(FakeRequest()).futureValue
      response.header.status shouldBe NOT_ACCEPTABLE
    }
  }

  "validating fromDate" should {
    "should use default value if fromDate is omitted" in {
      liveFractionsController.validateFromDate(None) shouldBe new LocalDate().minusMonths(liveFractionsController.defaultPriorMonthsForFromDate)
    }
    "use date if supplied" in {
      val date: LocalDate = new LocalDate("2013-07-22")
      liveFractionsController.validateToDate(Some(date)) shouldBe date
    }
  }

  "validating toDate" should {
    "should use default value if toDate is omitted" in {
      liveFractionsController.validateToDate(None) shouldBe new LocalDate()
    }
    "use date if supplied" in {
      val date: LocalDate = new LocalDate("2010-08-03")
      liveFractionsController.validateToDate(Some(date)) shouldBe date
    }
  }
}
