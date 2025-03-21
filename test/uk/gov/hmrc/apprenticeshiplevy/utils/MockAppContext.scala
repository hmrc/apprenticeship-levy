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

package uk.gov.hmrc.apprenticeshiplevy.utils

import uk.gov.hmrc.apprenticeshiplevy.config.AppContext
import org.mockito.Mockito.when
import play.api.Mode

object MockAppContext extends AppLevyUnitSpec {
  val mocked = mock[AppContext]

  when(mocked.mode).thenReturn(Mode.Test)

  def reset() = {
    org.mockito.Mockito.reset(mocked)
    when(mocked.mode).thenReturn(Mode.Test)
  }
}
