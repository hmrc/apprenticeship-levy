/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, contentAsJson, defaultAwaitTimeout}
import uk.gov.hmrc.apprenticeshiplevy.utils.AppLevyUnitSpec
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, TruncationLog}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import java.time.Instant
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
  val requestHeader: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)

  def versionHeader: (String, String) = ACCEPT -> s"application/vnd.hmrc.1.0+json"

  "onClientError" should {
    "return the appropriate status code" when {
      "a NOT_FOUND error is flagged" in {

        val result = handler.onClientError(requestHeader, NOT_FOUND, "Not Found")

        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":404,"message":"URI not found","requested":"/"}""")
      }
      "a BAD_REQUEST error is flagged" in {

        val result = handler.onClientError(requestHeader, BAD_REQUEST, "Bad Request")

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":400,"message":"Bad Request"}""")
      }
      "another error is flagged" in {
        val result = handler.onClientError(requestHeader, INTERNAL_SERVER_ERROR, "Other Error")

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsJson(result) shouldBe Json.parse("""{"statusCode":500,"message":"Other Error"}""")
      }
    }
  }

}
