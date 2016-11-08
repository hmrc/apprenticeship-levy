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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.LocalDate

sealed trait FinalSubmission
case class CombinedFinalSubmission(schemeCeased: Option[Boolean], schemeCeasedDate: Option[LocalDate], forYear: Option[Boolean]) extends FinalSubmission
case class SchemeCeased(schemeCeased: Boolean, schemeCeasedDate: LocalDate, forYear: Option[Boolean]) extends FinalSubmission
case class LastSubmission(forYear: Boolean) extends FinalSubmission

object FinalSubmission {
  implicit val combinedFinalSubmissionReads: Reads[FinalSubmission] = (
    (JsPath \ "schemeCeased").readNullable[Boolean] and
    (JsPath \ "schemeCeasedDate").readNullable[LocalDate] and
    (JsPath \ "forYear").readNullable[Boolean]
  )(FinalSubmission.apply _)

  implicit val combinedFinalSubmissionWrites: Writes[FinalSubmission] = (
    (JsPath \ "schemeCeased").write[Option[Boolean]] and
    (JsPath \ "schemeCeasedDate").write[Option[LocalDate]] and
    (JsPath \ "forYear").write[Option[Boolean]]
  )(FinalSubmission.unapply _)

  def unapply(fs: FinalSubmission): (Option[Boolean], Option[LocalDate], Option[Boolean]) = fs match {
    case LastSubmission(fy) => (None, None, Some(fy))
    case SchemeCeased(sc, scd, fy) => (Some(sc), Some(scd), fy)
    case CombinedFinalSubmission(sc, scd, fy) => (sc, scd, fy)
  }

  def apply(schemeCeased: Option[Boolean],
            schemeCeasedDate: Option[LocalDate],
            forYear: Option[Boolean]): FinalSubmission = {
    CombinedFinalSubmission(schemeCeased, schemeCeasedDate, forYear) match {
      case CombinedFinalSubmission(None, None, Some(forYear)) => LastSubmission(forYear)
      case CombinedFinalSubmission(Some(sc), Some(scd), fy) => SchemeCeased(sc, scd, fy)
      case CombinedFinalSubmission(sc, scd, fy) => CombinedFinalSubmission(sc, scd, fy)
    }
  }
}

