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

import generators.Generators
import models.EuTaxIdentifierType.Vat
import models.{Country, EuTaxIdentifier}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class EuTaxRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "EU Tax Registration" - {

    "must serialise and deserialise from / to an EU VAT Registration" in {

      val euVatNumberGen = arbitrary[Int].map(_.toString)

      forAll(arbitrary[Country], euVatNumberGen) {
        case (country, vatNumber) =>

          val euVatRegistration = EuVatRegistration(country, vatNumber)

          val json = Json.toJson(euVatRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euVatRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

      forAll(arbitrary[Country], arbitrary[TradeDetails], arbitrary[EuTaxIdentifier]) {
        case (country, fixedEstablishment, taxRef) =>

          val euRegistration = RegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

      forAll(arbitrary[Country]) {
        country =>
          val euRegistration = RegistrationWithoutTaxId(country)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }
  }

  "EuVatRegistration" - {

    "must deserialise/serialise to and from EuVatRegistration" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> "ATU123456789"
      )

      val expectedResult = EuVatRegistration(
        country = Country("AT", "Austria"),
        vatNumber = "ATU123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EuVatRegistration] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> 12345
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "vatNumber" -> JsNull
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }
  }

  "RegistrationWithFixedEstablishment" - {

    "must deserialise/serialise to and from RegistrationWithFixedEstablishment" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = RegistrationWithFixedEstablishment(
        country = Country("AT", "Austria"),
        taxIdentifier = EuTaxIdentifier(
          identifierType = Vat,
          value = "123456789"
        ),
        fixedEstablishment = TradeDetails(
          tradingName = "Trading Name",
          address = InternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("DE", "Germany")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithFixedEstablishment] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> 12345,
              "name" -> "Germany"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> JsNull
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }
  }

  "RegistrationWithoutFixedEstablishmentWithTradeDetails" - {

    "must deserialise/serialise to and from RegistrationWithoutFixedEstablishmentWithTradeDetails" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = RegistrationWithoutFixedEstablishmentWithTradeDetails(
        country = Country("AT", "Austria"),
        taxIdentifier = EuTaxIdentifier(
          identifierType = Vat,
          value = "123456789"

        ),
        tradeDetails = TradeDetails(
          tradingName = "Trading Name",
          address = InternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("DE", "Germany")
          )
        )

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> 12345,
              "name" -> "Germany"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> JsNull
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }
  }

  "RegistrationWithoutFixedEstablishment" - {

    "must deserialise/serialise to and from RegistrationWithoutFixedEstablishment" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        )
      )

      val expectedResult = RegistrationWithoutFixedEstablishment(
        country = Country("AT", "Austria"),
        taxIdentifier = EuTaxIdentifier(
          identifierType = Vat,
          value = "123456789"
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutFixedEstablishment] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutFixedEstablishment] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Austria"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        )
      )

      json.validate[RegistrationWithoutFixedEstablishment] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> JsNull
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        )
      )

      json.validate[RegistrationWithoutFixedEstablishment] mustBe a[JsError]
    }
  }

  "RegistrationWithoutTaxId" - {

    "must deserialise/serialise to and from RegistrationWithoutTaxId" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> "Austria"
        )
      )

      val expectedResult = RegistrationWithoutTaxId(
        country = Country("AT", "Austria")
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutTaxId] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "AT",
          "name" -> 12345
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Austria"
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }
  }
}
