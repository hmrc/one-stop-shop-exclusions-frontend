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

import base.SpecBase
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class VatReturnSpec extends SpecBase {

  private val period: Period = arbitraryStandardPeriod.arbitrary.sample.value

  "VatReturn" - {

    "must serialise/deserialise to and from VatReturn" in {

      val json = Json.obj(
        "period" -> period
      )

      val expectedResult: VatReturn = VatReturn(
        period = period
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[VatReturn] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[VatReturn] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "period" -> Json.obj(
          "year" -> 2067,
          "quarter" -> 12345
        )
      )

      json.validate[VatReturn] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "period" -> Json.obj(
          "year" -> 2067,
          "quarter" -> JsNull
        )
      )

      json.validate[VatReturn] mustBe a[JsError]
    }
  }
}
