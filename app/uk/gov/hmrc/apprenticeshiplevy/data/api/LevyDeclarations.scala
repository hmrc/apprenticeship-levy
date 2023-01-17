/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.data.api

import play.api.libs.json._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

case class PayrollPeriod(year: String, month: Int)

object PayrollPeriod {
  implicit val formats = Json.format[PayrollPeriod]
}

case class LevyDeclaration(id: Long,
                           submissionTime: LocalDateTime,
                           dateCeased: Option[LocalDate] = None,
                           inactiveFrom: Option[LocalDate] = None,
                           inactiveTo: Option[LocalDate] = None,
                           payrollPeriod: Option[PayrollPeriod] = None,
                           levyDueYTD: Option[BigDecimal] = None,
                           levyAllowanceForFullYear: Option[BigDecimal] = None,
                           noPaymentForPeriod: Option[Boolean] = None,
                           submissionId: Long = 0L)


object LevyDeclaration {
  implicit val ldtFormats = new Format[LocalDateTime] {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    override def reads(json: JsValue): JsResult[LocalDateTime] = implicitly[Reads[JsString]].reads(json).map { js =>
      LocalDateTime.parse(js.value, fmt)
    }

    override def writes(o: LocalDateTime): JsValue = JsString(o.format(fmt))
  }

  implicit val formats = Json.format[LevyDeclaration]
}

case class LevyDeclarations(empref: String, declarations: Seq[LevyDeclaration])

object LevyDeclarations {
  implicit val formats = Json.format[LevyDeclarations]
}
