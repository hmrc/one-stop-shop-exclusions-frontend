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

package models.registration

import models.registration.VatDetailSource.Etmp
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.LocalDate

class VatDetailsSpec extends AnyFreeSpec with Matchers {

  "VatDetails" - {

    "must deserialise/serialise to and from VatDetails" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "registrationDate" -> LocalDate.now(),
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            ),
            "line2" -> "Line 2",
            "county" -> "County"
          ),
          "partOfVatGroup" -> false,
          "source" -> "etmp"
        )

        val expectedResult = VatDetails(
          registrationDate = LocalDate.now(),
          address = UkAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            county = Some("County"),
            postCode = "Postcode"
          ),
          partOfVatGroup = false,
          source = Etmp
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[VatDetails] mustBe JsSuccess(expectedResult)
      }

      "when optional values are missing" in {

        val json = Json.obj(
          "registrationDate" -> LocalDate.now(),
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          ),
          "partOfVatGroup" -> false,
          "source" -> "etmp"
        )

        val expectedResult = VatDetails(
          registrationDate = LocalDate.now(),
          address = UkAddress(
            line1 = "Line 1",
            line2 = None,
            townOrCity = "Town",
            county = None,
            postCode = "Postcode"
          ),
          partOfVatGroup = false,
          source = Etmp
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[VatDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[VatDetails] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "registrationDate" -> "2025-02-17",
        "address" -> Json.obj(
          "line1" -> 12345,
          "townOrCity" -> "Town",
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          ),
          "line2" -> "Line 2",
          "county" -> "County"
        ),
        "partOfVatGroup" -> false,
        "source" -> "etmp"
      )

      json.validate[VatDetails] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "registrationDate" -> "2025-02-17",
        "address" -> Json.obj(
          "line1" -> JsNull,
          "townOrCity" -> "Town",
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          ),
          "line2" -> "Line 2",
          "county" -> "County"
        ),
        "partOfVatGroup" -> false,
        "source" -> "etmp"
      )

      json.validate[VatDetails] mustBe a[JsError]
    }
  }
}
