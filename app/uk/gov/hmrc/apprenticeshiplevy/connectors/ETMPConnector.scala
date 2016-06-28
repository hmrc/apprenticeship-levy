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
import play.api.libs.json.Json
import uk.gov.hmrc.apprenticeshiplevy.config.{AppContext, WSHttp}
import uk.gov.hmrc.apprenticeshiplevy.data.PayrollMonth
import uk.gov.hmrc.apprenticeshiplevy.data.charges.Charges
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}
import views.html.helper
import com.github.nscala_time.time.Imports._

import scala.concurrent.Future

case class ETMPLevyDeclaration(payrollMonth: PayrollMonth, amount: BigDecimal, submissionType: String, submissionDate: String)

object ETMPLevyDeclaration {
  implicit val formats = Json.format[ETMPLevyDeclaration]
}

case class ETMPLevyDeclarations(empref: String, schemeCessationDate: Option[LocalDate], declarations: Seq[ETMPLevyDeclaration])

object ETMPLevyDeclarations {
  implicit val formats = Json.format[ETMPLevyDeclarations]
}

// Starting year must be 2xxx - at some point we'll enforce this
case class TaxYear(startingYear: Int) extends AnyVal {
  def stringForETMP = s"${startingYear}_${startingYear - 1999}"

  def next: TaxYear = TaxYear(startingYear + 1)

  def endDate: LocalDate = new LocalDate(startingYear + 1, 4, 5)
}

object TaxYear {
  def yearsInRange(startDate: LocalDate, endDate: LocalDate): Seq[TaxYear] = {
    if (startDate > endDate) Seq()
    else forDate(startDate) +: yearsInRange(startDate.plusYears(1), endDate)
  }

  def forDate(date: LocalDate): TaxYear = {
    val refDate = new LocalDate(date.getYear, 4, 6)
    if (date < refDate) TaxYear(date.getYear - 1)
    else TaxYear(date.getYear)
  }
}

trait ETMPConnector {

  def etmpBaseUrl: String

  def httpGet: HttpGet

  def declarations(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[ETMPLevyDeclarations] = {
    val url = (s"$etmpBaseUrl/empref/${helper.urlEncode(empref)}/declarations", months) match {
      case (u, Some(n)) => s"$u/?months=$n"
      case (u, None) => u
    }

    Logger.debug(s"Calling ETMP at $url")

    httpGet.GET[ETMPLevyDeclarations](url)
  }

  def charges(empref: String, taxYear: TaxYear)(implicit hc: HeaderCarrier): Future[Charges] = {
    val url = s"$etmpBaseUrl/pay-as-you-earn/employers/$empref/charges/taxyear/${taxYear.stringForETMP}"

    httpGet.GET[Charges](url)
  }
}

object LiveETMPConnector extends ETMPConnector {
  override def etmpBaseUrl = AppContext.etmpUrl

  override def httpGet = WSHttp
}

object SandboxETMPConnector extends ETMPConnector {
  override def etmpBaseUrl = AppContext.stubEtmpUrl

  override def httpGet = WSHttp
}
