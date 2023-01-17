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

package uk.gov.hmrc.apprenticeshiplevy.utils

import com.google.inject.Singleton

import scala.annotation.tailrec
import scala.util.Random

@Singleton
class DataTransformer() {

  def transform(input: String): String = {
    randomizeSubmissionIds(input)
  }

  private def randomizeSubmissionIds(input: String): String = {
    incrementalReplace(input, "{randomSubmissionId}", randomSubmissionId)
  }

  @tailrec
  private def incrementalReplace(content: String, search: String, replacement: => String): String = {
    val index = content.indexOf(search)
    if(index >= 0) {
      incrementalReplace(content.take(index) + replacement + content.drop(index + search.length), search, replacement)
    } else {
      content
    }
  }

  private def randomSubmissionId = "9" + randomDigits(10)
  private def randomDigits(digits: Int) = {
    @tailrec
    def loop(digits: Int, result: String): String = {
      if(digits < 1) result
      else loop(digits - 1, result + Random.nextInt(9))
    }

    loop(digits, "")
  }
}

