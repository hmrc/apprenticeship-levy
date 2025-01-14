/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.apprenticeshiplevy.http

import java.time.Instant
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, contentAsJson, defaultAwaitTimeout}
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, TruncationLog}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.ExecutionContext.Implicits.global

class CustomErrorHandlerSpec extends AppLevyUnitSpec {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockHttpAuditEvent: HttpAuditEvent = mock[HttpAuditEvent]
  val configuration: Configuration = Configuration(
    "appName"                                         -> "myApp",
    "bootstrap.errorHandler.warnOnly.statusCodes"     -> List.empty,
    "bootstrap.errorHandler.suppress4xxErrorMessages" -> false,
    "bootstrap.errorHandler.suppress5xxErrorMessages" -> false
  )

  val eventTags: Map[String, String] = Map("transactionName" -> "event.transactionName")

  val dataEvent: DataEvent = DataEvent(
    auditSource = "auditSource",
    auditType = "event.auditType",
    eventId = "",
    tags = eventTags,
    detail = Map("test" -> "test"),
    generatedAt = Instant.now(),
    truncationLog = TruncationLog.Empty
  )

  val handler = new CustomErrorHandler(
    auditConnector = mockAuditConnector,
    httpAuditEvent = mockHttpAuditEvent,
    configuration = configuration
  )
  def versionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"
  val requestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)

  "onClientError" should {
    "return the appropriate status code" when {
      "a NOT_FOUND error is flagged" in {

        val result = handler.onClientError(requestHeader, 404, "Not Found")

        status(result) shouldBe 404
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":404,"message":"URI not found","requested":"/"}""")
      }
      "a BAD_REQUEST error is flagged" in {

        val result = handler.onClientError(requestHeader, 400, "Bad Request")

        status(result) shouldBe 400
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":400,"message":"Bad Request"}""")
      }
      "another error is flagged" in {
        val result = handler.onClientError(requestHeader, 500, "Other Error")

        status(result) shouldBe 500
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":500,"message":"Other Error"}""")
      }
    }
  }

}
