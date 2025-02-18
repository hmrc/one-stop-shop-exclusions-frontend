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
import play.api.libs.json.{JsError, JsNull, JsObject, JsSuccess, Json}


class PreviousRegistrationSpec extends AnyWordSpec with Matchers {

  "PreviousRegistration" must {

    "serialize and deserialize PreviousRegistrationNew correctly" in {
      val previousRegistrationNew = PreviousRegistrationNew(
        country = Country("AT", "Austria"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSU,
            previousSchemeNumbers = PreviousSchemeNumbers("12345", Some("98765"))
          )
        )
      )

      val json = Json.obj(
        "country" -> Json.obj (
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345",
              "previousIntermediaryNumber" -> "98765"
            )
          )
        )
      )

      Json.toJson(previousRegistrationNew) mustEqual json

      json.as[PreviousRegistration] mustEqual previousRegistrationNew
    }

    "serialize and deserialize PreviousRegistrationLegacy correctly" in {
      val previousRegistrationLegacy = PreviousRegistrationLegacy(
        country = Country("AT", "Austria"),
        vatNumber = "ATU123456789"
      )

      val json = Json.obj(
        "country" -> Json.obj (
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> "ATU123456789"
      )

      Json.toJson(previousRegistrationLegacy) mustEqual json

      json.as[PreviousRegistration] mustEqual previousRegistrationLegacy
    }

    "serialize and deserialize PreviousRegistration correctly (polymorphic)" in {
      val previousRegistrationNew = PreviousRegistrationNew(
        country = Country("AT", "Austria"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSU,
            previousSchemeNumbers = PreviousSchemeNumbers("12345", Some("98765"))
          )
        )
      )

      val jsonNew = Json.obj(
        "country" -> Json.obj (
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345",
              "previousIntermediaryNumber" -> "98765"
            )
          )
        )
      )

      Json.toJson(previousRegistrationNew) mustEqual jsonNew

      jsonNew.as[PreviousRegistration] mustEqual previousRegistrationNew

      val previousRegistrationLegacy = PreviousRegistrationLegacy(
        country = Country("AT", "Austria"),
        vatNumber = "ATU123456789"
      )

      val jsonLegacy = Json.obj(
        "country" -> Json.obj (
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> "ATU123456789"
      )


      Json.toJson(previousRegistrationLegacy) mustEqual jsonLegacy

      jsonLegacy.as[PreviousRegistration] mustEqual previousRegistrationLegacy
    }

  }

  "PreviousRegistrationNew" must {

    "must deserialise/serialise to and from PreviousRegistrationNew" in {
      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345",
              "previousIntermediaryNumber" -> "98765"
            )
          )
        )
      )

      val expectedResult = PreviousRegistrationNew(
        country = Country("DE", "Germany"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSU,
            previousSchemeNumbers = PreviousSchemeNumbers("12345", Some("98765"))
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationNew] mustBe JsSuccess(expectedResult)
    }

    "must handle optional fields when deserialise/serialise" in {
      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345"
            )
          )
        )
      )

      val expectedResult = PreviousRegistrationNew(
        country = Country("DE", "Germany"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSU,
            previousSchemeNumbers = PreviousSchemeNumbers("12345", None)
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationNew] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345",
              "previousIntermediaryNumber" -> "98765"
            )
          )
        )
      )

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> JsNull
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "12345",
              "previousIntermediaryNumber" -> "98765"
            )
          )
        )
      )

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }
  }

  "PreviousRegistrationLegacy" must {

    "must deserialise/serialise to and from PreviousRegistrationLegacy" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> "ATU123456789"
      )

      val expectedResult = PreviousRegistrationLegacy(
        country = Country("AT", "Austria"),
        vatNumber = "ATU123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationLegacy] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Austria"
        ),
        "vatNumber" -> "ATU123456789"
      )

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> JsNull
        ),
        "vatNumber" -> "ATU123456789"
      )

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }
  }

  "PreviousSchemeDetails" must {

    "must deserialise/serialise to and from TradeDetails" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "12345",
          "previousIntermediaryNumber" -> "98765"
        )
      )

      val expectedResult = PreviousSchemeDetails(
        previousScheme = PreviousScheme.OSSU,
        previousSchemeNumbers = PreviousSchemeNumbers("12345", Some("98765"))
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousSchemeDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle optional values when deserialise/serialise to and from TradeDetails" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "12345"
        )
      )

      val expectedResult = PreviousSchemeDetails(
        previousScheme = PreviousScheme.OSSU,
        previousSchemeNumbers = PreviousSchemeNumbers("12345", None)
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousSchemeDetails] mustBe JsSuccess(expectedResult)
    }


    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> 12345,
          "previousIntermediaryNumber" -> "98765"
        )
      )

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> JsNull,
          "previousIntermediaryNumber" -> "98765"
        )
      )

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }
  }

  "PreviousSchemeNumbers" must {

    "must deserialise/serialise to and from TradeDetails" in {

      val json = Json.obj(
        "previousSchemeNumber" -> "12345",
        "previousIntermediaryNumber" -> "98765"
      )

      val expectedResult = PreviousSchemeNumbers(
        previousSchemeNumber = "12345",
        previousIntermediaryNumber = Some("98765")
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousSchemeNumbers] mustBe JsSuccess(expectedResult)
    }

    "must handle optional values when deserialise/serialise to and from TradeDetails" in {

      val json = Json.obj(
        "previousSchemeNumber" -> "12345",
      )

      val expectedResult = PreviousSchemeNumbers(
        previousSchemeNumber = "12345",
        previousIntermediaryNumber = None
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousSchemeNumbers] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "previousSchemeNumber" -> 12345,
        "previousIntermediaryNumber" -> "98765"
      )

      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "previousSchemeNumber" -> JsNull,
        "previousIntermediaryNumber" -> "98765"
      )

      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }
  }
}