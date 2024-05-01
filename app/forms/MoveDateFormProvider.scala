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

package forms

import date.{Dates, LocalDateOps}
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.LocalDate
import javax.inject.Inject

class MoveDateFormProvider @Inject()(dates: Dates) extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "moveDate.error.invalid",
        allRequiredKey = "moveDate.error.required.all",
        twoRequiredKey = "moveDate.error.required.two",
        requiredKey    = "moveDate.error.required"
      ).verifying(validDate)
    )

  private def validDate: Constraint[LocalDate] = Constraint {
    case date if date < dates.firstDayOfQuarter => Invalid("moveDate.error.invalid.minDate", dates.formatter.format(dates.firstDayOfQuarter))
    case date if date > dates.lastDayOfQuarter => Invalid("moveDate.error.invalid.maxDate", dates.formatter.format(dates.lastDayOfQuarter))
    case _ => Valid
  }
}
