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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDateTime, ZoneOffset}

class IntervalSpec extends AnyWordSpec with Matchers {

  val zoneOffset = ZoneOffset.UTC

  "intervals" must {
    "have an overlap between two intervals where instants overlap" in {

      val intervalA = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val intervalB = Interval(
        LocalDateTime.of(2022, 5, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 7, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val overlap = intervalA.overlaps(intervalB)

      overlap mustBe true
    }

    "have no overlap between two intervals where instants do not overlap" in {
      val intervalA = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val intervalB = Interval(
        LocalDateTime.of(2022, 7, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 8, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val overlap = intervalA.overlaps(intervalB)

      overlap mustBe false
    }

    "have a containing instant within an interval when instant is contained within in interval" in {
      val interval = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val instant = LocalDateTime.of(2022, 5, 1, 0, 0, 0).toInstant(zoneOffset)

      val contains = interval.contains(instant)

      contains mustBe true
    }

    "have a containing instant when instant equals start of interval" in {
      val interval = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val instant = LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset)

      val contains = interval.contains(instant)

      contains mustBe true
    }

    "have a containing instant when instant equals end of interval" in {
      val interval = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val instant = LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)

      val contains = interval.contains(instant)

      contains mustBe true
    }

    "not have a containing instant within an interval when instant is not contained within in interval" in {
      val interval = Interval(
        LocalDateTime.of(2022, 4, 1, 0, 0, 0).toInstant(zoneOffset),
        LocalDateTime.of(2022, 6, 1, 0, 0, 0).toInstant(zoneOffset)
      )

      val instant = LocalDateTime.of(2022, 3, 1, 0, 0, 0).toInstant(zoneOffset)

      val contains = interval.contains(instant)

      contains mustBe false
    }
  }
}
