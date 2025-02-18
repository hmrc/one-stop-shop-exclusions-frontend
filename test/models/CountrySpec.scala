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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class CountrySpec extends AnyFreeSpec with Matchers {

  "Country" - {

    "serialize and deserialize correctly" in {
      val country = Country("DE", "Germany")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "DE",
        "name" -> "Germany"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "serialize and deserialize with another Country" in {
      val country = Country("FR", "France")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "FR",
        "name" -> "France"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "handle invalid JSON" in {
      val invalidJson = Json.obj(
        "code" -> "IT",
      )

      val result = invalidJson.validate[Country]
      result.isError mustBe true
    }

    "handle missing 'code' field" in {
      val invalidJson = Json.obj(
        "name" -> "Germany"
      )

      val result = invalidJson.validate[Country]
      result.isError mustBe true
    }

    "serialize a Country with a long name" in {
      val country = Country("TH", "Thailand")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "TH",
        "name" -> "Thailand"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "deserialize a Country with unknown code" in {
      val unknownJson = Json.obj(
        "code" -> "ZZ",
        "name" -> "UnknownLand"
      )

      val deserialized = unknownJson.as[Country]
      deserialized mustEqual Country("ZZ", "UnknownLand")
    }
  }

  "CountryWithValidationDetails" - {

    "should contain validation rules for all EU countries" in {
      val countryCodesWithValidation = CountryWithValidationDetails.euCountriesWithVRNValidationRules.map(_.country.code).toSet
      val expectedCountryCodes = Country.euCountries.map(_.code).toSet

      countryCodesWithValidation mustBe expectedCountryCodes
    }

    "should correctly validate a VAT number format" in {
      val germanyDetails = CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == "DE").get
      "DE123456789" must fullyMatch regex germanyDetails.vrnRegex
    }

    "should correctly convert a tax identifier for transfer" in {
      val result = CountryWithValidationDetails.convertTaxIdentifierForTransfer("DE123456789", "DE")
      result mustBe "123456789"
    }

    "should throw an exception for an unknown country code" in {
      val exception = intercept[IllegalStateException] {
        CountryWithValidationDetails.convertTaxIdentifierForTransfer("XX123456789", "XX")
      }
      exception.getMessage must include("unable to convert identifier")
    }

    "should not modify an invalid VAT number" in {
      val result = CountryWithValidationDetails.convertTaxIdentifierForTransfer("INVALID123", "DE")
      result mustBe "INVALID123"
    }
  }

}
