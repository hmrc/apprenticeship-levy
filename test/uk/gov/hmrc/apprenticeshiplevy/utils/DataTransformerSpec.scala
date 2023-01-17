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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DataTransformerSpec extends AnyWordSpec with Matchers {

  val sut = new DataTransformer()

  "transform" should {

    val input = "{randomSubmissionId}," * 100
    val result = sut.transform(input).split(',')

    "produce 100 results" in { result.length shouldBe 100 }

    "produce 11 characters per result" in { result.forall(_.length == 11) shouldBe true }

    "produce numbers" in { result.forall(_.toList.forall(_.isDigit)) shouldBe true }

    "produce distinct values" in { result.distinct.length shouldBe 100 }

    "produce values beginning with 9" in { result.forall(_.head == '9') shouldBe true }
  }
} 