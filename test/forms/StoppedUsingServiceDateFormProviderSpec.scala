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

import date.{Dates, Today, TodayImpl}

import java.time.LocalDate
import forms.behaviours.DateBehaviours
import org.scalatestplus.mockito.MockitoSugar.mock

class StoppedUsingServiceDateFormProviderSpec extends DateBehaviours {

  val mockToday: Today = mock[Today]
  val dates = new Dates(new TodayImpl(Dates.clock))

  ".value" - {

    val commencementDate = LocalDate.parse("2013-12-03")
    val currentDate = LocalDate.parse("2013-12-01")
    val endOfPeriod = LocalDate.parse("2013-12-31")

    val form = new StoppedUsingServiceDateFormProvider(dates)(currentDate, commencementDate)

    val validData = datesBetween(
      min = commencementDate,
      max = endOfPeriod
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "stoppedUsingServiceDate.error.required.all")

  }
}
