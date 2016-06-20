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

package uk.gov.hmrc.apprenticeshiplevy

import play.api.mvc.Results._

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
 *
 */

/**
  * Created by jim on 11/05/2016.
  */
package object controllers {
  val CODE_UNAUTHORIZED = "UNAUTHORIZED"
  val CODE_INVALID_TAX_YEAR = "ERROR_TAX_YEAR_INVALID"
  val CODE_INVALID_EMP_REF = "ERROR_EMP_REF_INVALID"
  val CODE_BAD_REQUEST = "BAD_REQUEST"
  val CODE_NOT_FOUND = "NOT_FOUND"
  val CODE_INVALID_HEADER = "ACCEPT_HEADER_INVALID"
  val CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
  val CODE_NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
  val CODE_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
}
