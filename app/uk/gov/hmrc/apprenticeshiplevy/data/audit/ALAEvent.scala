/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.data.audit

import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.http.HeaderCarrier

case class ALAEvent(name: String, empref: String = "", nino: String = "", dateRange: String = "") {
  protected lazy val data: Map[String, String] = Seq(("empref",empref), ("nino",nino), ("dateRange",dateRange)).filterNot(_._2.isEmpty).toMap

  def toDataEvent(httpStatus: Int)(implicit hc: HeaderCarrier): DataEvent = DataEvent("ala-api",
     "ServiceReceivedRequest",
     tags = hc.toAuditTags(name, ""),
     detail = data ++ Map("upstream_http_status"->httpStatus.toString))

  def toDataEvent(httpStatus: Int, exception: Throwable)(implicit hc: HeaderCarrier): DataEvent = DataEvent("ala-api",
    "ServiceReceivedRequest",
    tags = hc.toAuditTags(name, ""),
    detail = data ++ Map("upstream_http_status"->httpStatus.toString) ++ Map("exception" → exception.toString))

}
