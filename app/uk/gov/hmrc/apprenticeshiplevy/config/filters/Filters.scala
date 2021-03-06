/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.Configuration
import play.api.http.DefaultHttpFilters
import play.filters.headers.{SecurityHeadersConfig, SecurityHeadersFilter}
import uk.gov.hmrc.play.bootstrap.backend.filters.BackendFilters

class Filters @Inject()(defaultFilters: BackendFilters,
                        apiHeaderCaptureFilter: APIHeaderCaptureFilter,
                        configuration: Configuration)
  extends DefaultHttpFilters(defaultFilters.filters :+ apiHeaderCaptureFilter
    :+ new SecurityHeadersFilter(SecurityHeadersConfig.fromConfiguration(configuration)): _*)

