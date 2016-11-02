/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.connectors

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.http.HttpGet
import org.joda.time.LocalDate
import uk.gov.hmrc.apprenticeshiplevy.data.des._
import scala.concurrent.Future
import uk.gov.hmrc.play.http.{HeaderCarrier,HttpReads,HttpResponse}
import uk.gov.hmrc.play.http.hooks.HttpHook
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.apprenticeshiplevy.utils._

class EDHConnectorSpec extends UnitSpec with MockitoSugar {
  "EDH Connector" should {
    "for Fraction Date endpoint" must {
      "when EDH not failing return local date instance of date" in {
        // set up
        val stubHttpGet = mock[HttpGet]
        when(stubHttpGet.GET[FractionCalculationDate](anyString())(any(), any())).thenReturn(Future.successful(FractionCalculationDate(new LocalDate(2016,11,3))))
        val connector = new EDHConnector() {
          def edhBaseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
        }

        // test
        val futureResult = connector.fractionCalculationDate(HeaderCarrier())

        // check
        await[LocalDate](futureResult) shouldBe new LocalDate(2016,11,3)
      }
    }
    "for Fractions endpoint" must {
      "when EDH not failing return fractions" in {
        // set up
        val stubHttpGet = mock[HttpGet]
        val expected = Fractions("123AB12345", List(FractionCalculation(new LocalDate(2016,4,22), List(Fraction("England", BigDecimal(0.83))))))
        when(stubHttpGet.GET[Fractions](anyString())(any(), any()))
           .thenReturn(Future.successful(expected))
        val connector = new EDHConnector() {
          def edhBaseUrl: String = "http://a.guide.to.nowhere/"
          def httpGet: HttpGet = stubHttpGet
        }

        // test
        val futureResult = connector.fractions("123AB12345", OpenDateRange)(HeaderCarrier())

        // check
        await[Fractions](futureResult) shouldBe expected
      }
    }
  }
}
