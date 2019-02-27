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

package uk.gov.hmrc.apprenticeshiplevy.utils

import org.joda.time.LocalDate

sealed trait DateRange {
  //def contains(date: LocalDate): Boolean

  def toParams: Option[String]
}

case object OpenDateRange extends DateRange {
  //override def contains(date: LocalDate): Boolean = true

  override def toParams: Option[String] = None
}

case class ClosedDateRange(from: LocalDate, to: LocalDate) extends DateRange {
  //override def contains(date: LocalDate): Boolean = !date.isBefore(from) && !date.isAfter(to)

  override def toParams: Option[String] = Some(paramString)

  def paramString: String = s"fromDate=$from&toDate=$to"
}

case class OpenEarlyDateRange(to: LocalDate) extends DateRange {
  //override def contains(date: LocalDate): Boolean = !date.isAfter(to)

  override def toParams: Option[String] = Some(s"toDate=$to")
}

case class OpenLateDateRange(from: LocalDate) extends DateRange {
  //override def contains(date: LocalDate): Boolean = !date.isBefore(from)

  override def toParams: Option[String] = Some(s"fromDate=$from")
}

object DateRange {
  def apply(fromDate: Option[LocalDate], toDate: Option[LocalDate]): DateRange = {
    (fromDate, toDate) match {
      case (None, None) => OpenDateRange
      case (Some(from), Some(to)) => ClosedDateRange(from, to)
      case (None, Some(to)) => OpenEarlyDateRange(to)
      case (Some(from), None) => OpenLateDateRange(from)
    }
  }
}
