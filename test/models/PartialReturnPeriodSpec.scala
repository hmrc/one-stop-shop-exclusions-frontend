/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

class PartialReturnPeriodSpec extends AnyWordSpec with Matchers {

  "PartialReturnPeriod" must {

    "serialize to JSON correctly" in {
      val partialReturnPeriod = PartialReturnPeriod(
        firstDay = LocalDate.of(2024, 1, 1),
        lastDay = LocalDate.of(2024, 3, 31),
        year = 2024,
        quarter = Quarter.Q1
      )

      val expectedJson: JsObject = Json.obj(
        "firstDay" -> "2024-01-01",
        "lastDay" -> "2024-03-31",
        "year" -> 2024,
        "quarter" -> "Q1"
      )

      Json.toJson(partialReturnPeriod) mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "firstDay" -> "2024-01-01",
        "lastDay" -> "2024-03-31",
        "year" -> 2024,
        "quarter" -> "Q1", // Assuming the Quarter enum is serialized as a string
        "isPartial" -> true
      )

      val expectedPartialReturnPeriod = PartialReturnPeriod(
        firstDay = LocalDate.of(2024, 1, 1),
        lastDay = LocalDate.of(2024, 3, 31),
        year = 2024,
        quarter = Quarter.Q1
      )

      json.as[PartialReturnPeriod] mustEqual expectedPartialReturnPeriod
    }

    "correctly set isPartial to true" in {
      val partialReturnPeriod = PartialReturnPeriod(
        firstDay = LocalDate.of(2024, 1, 1),
        lastDay = LocalDate.of(2024, 3, 31),
        year = 2024,
        quarter = Quarter.Q1
      )

      partialReturnPeriod.isPartial mustEqual true
    }
  }
}
