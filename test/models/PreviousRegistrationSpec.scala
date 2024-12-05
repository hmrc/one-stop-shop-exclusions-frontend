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
}