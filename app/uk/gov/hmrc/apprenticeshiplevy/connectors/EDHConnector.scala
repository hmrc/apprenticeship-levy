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

import org.joda.time.LocalDate
import play.api.Logger
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.Fractions
import uk.gov.hmrc.apprenticeshiplevy.data.des.FractionCalculationDate
import uk.gov.hmrc.apprenticeshiplevy.utils.DateRange
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

trait EDHConnector {

  def edhBaseUrl: String

  def httpGet: HttpGet

  def fractions(empref: String, dateRange: DateRange)(implicit hc: HeaderCarrier): Future[Fractions] = {
    val url = (s"$edhBaseUrl/empref/${helper.urlEncode(empref)}/fractions", dateRange.toParams) match {
      case (u, Some(params)) => s"$u?$params"
      case (u, None) => u
    }
    // $COVERAGE-OFF$
    Logger.debug(s"Calling EDH at $url")
    // $COVERAGE-ON$
    httpGet.GET[Fractions](url)
  }

  def fractionCalculationDate(implicit hc: HeaderCarrier): Future[LocalDate] = {
    val url = s"$edhBaseUrl/fraction-calculation-date"

    httpGet.GET[FractionCalculationDate](url) map {
      _.date
    }
  }
}

object LiveEDHConnector extends EDHConnector {
  override def edhBaseUrl: String = AppContext.edhUrl

  override def httpGet: HttpGet = WSHttp
}

object SandboxEDHConnector extends EDHConnector {
  override def edhBaseUrl: String = AppContext.stubEdhUrl

  override def httpGet: HttpGet = WSHttp
}
