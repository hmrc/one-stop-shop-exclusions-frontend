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

package models

import base.SpecBase
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class WebsiteSpec extends SpecBase {

  "Website" - {

    "must serialize to JSON correctly" in {

      val website = Website("http://example.com")

      val expectedJson = Json.obj(
        "websiteAddress" -> "http://example.com"
      )

      Json.toJson(website) mustBe expectedJson
    }

    "must deserialize from JSON correctly" in {

      val json = Json.obj(
        "websiteAddress" -> "http://example.com"
      )

      val expectedResult = Website("http://example.com")

      json.validate[Website] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[Website] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj("websiteAddress" -> 12345)

      json.validate[Website] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj("websiteAddress" -> JsNull)

      json.validate[Website] mustBe a[JsError]
    }
  }
}
