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

package forms.mappings

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.validation.{Constraint, Invalid, Valid}


class EuVatNumberConstraintsSpec extends AnyFreeSpec with Matchers with EuVatNumberConstraints {

  "validateEuVatNumber" - {
    "return Valid when VAT number matches country regex" in {
      val constraint: Constraint[String] = validateEuVatNumber("DE", "error.vat.invalid")
      constraint("DE123456789") mustBe Valid
    }

    "return Invalid when VAT number does not match country regex" in {
      val constraint: Constraint[String] = validateEuVatNumber("DE", "error.vat.invalid")
      constraint("INVALID123") mustBe Invalid("error.vat.invalid")
    }
  }

  "validateEuVatNumberOrEu" - {
    "return Valid when VAT number matches country regex" in {
      val constraint: Constraint[String] = validateEuVatNumberOrEu("FR", "error.vat.invalid")
      constraint("FR12345678901") mustBe Valid
    }

    "return Valid when VAT number is an EU registration number" in {
      val constraint: Constraint[String] = validateEuVatNumberOrEu("FR", "error.vat.invalid")
      constraint("EU123456789") mustBe Valid
    }

    "return Invalid when VAT number is invalid" in {
      val constraint: Constraint[String] = validateEuVatNumberOrEu("FR", "error.vat.invalid")
      constraint("INVALID") mustBe Invalid("error.vat.invalid")
    }
  }
}

