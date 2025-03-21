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

package uk.gov.hmrc.apprenticeshiplevy.config.filters

import org.apache.pekko.stream.Materializer
import com.google.inject.Inject
import org.slf4j.MDC
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

class APIHeaderCaptureFilter @Inject()(val mat: Materializer) extends Filter {
  def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    MDC.put("X-Client-ID",rh.headers.toSimpleMap.getOrElse("X-Client-ID","Unknown caller"))
    next(rh)
  }
}
