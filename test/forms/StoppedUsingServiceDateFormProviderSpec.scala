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
import org.mockito.MockitoSugar.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar.mock

class StoppedUsingServiceDateFormProviderSpec extends DateBehaviours {

  val mockToday: Today = mock[Today]
  val dates = new Dates(new TodayImpl(Dates.clock))
  private val form = new StoppedUsingServiceDateFormProvider(dates)()

  ".value" - {

    val minDate: LocalDate = dates.firstDayOfQuarter
    val maxDate: LocalDate = dates.lastDayOfQuarter

    val validData = datesBetween(
      min = minDate,
      max = maxDate
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "stoppedUsingServiceDate.error.required.all")

    "bind dates if today is within current quarter period" in {
      val todayGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2024, 2, 1),
        max = LocalDate.of(2024, 2, 28)
      )

      val validDatesGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2024, 1, 1),
        max = LocalDate.of(2024, 3, 31)
      )

      forAll(todayGen, validDatesGen) { (today, validDate) =>
        when(mockToday.date).thenReturn(today)
        val dates = new Dates(mockToday)
        val form = new MoveDateFormProvider(dates)()

        val data = formData(validDate)
        val result = form.bind(data)
        result.value.value mustEqual validDate
        result.errors mustBe empty
      }
    }

    "fail to bind date if today is before the current quarter period" in {

      val todayGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2023, 12, 1),
        max = LocalDate.of(2023, 12, 31)
      )

      val validDatesGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2024, 1, 1),
        max = LocalDate.of(2024, 3, 31)
      )

      forAll(todayGen, validDatesGen) { (today, validDate) =>
        when(mockToday.date).thenReturn(today)

        val dates = new Dates(mockToday)
        val form = new MoveDateFormProvider(dates)()

        val data = formData(validDate)
        val result = form.bind(data)
        result.errors must not be empty
      }
    }

    "fail to bind date if today is after the current quarter period" in {
      val todayGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2024, 7, 1),
        max = LocalDate.of(2024, 7, 31)
      )

      val validDatesGen: Gen[LocalDate] = datesBetween(
        min = LocalDate.of(2024, 1, 1),
        max = LocalDate.of(2024, 3, 31)
      )

      forAll(todayGen, validDatesGen) { (today, validDate) =>
        when(mockToday.date).thenReturn(today)

        val dates = new Dates(mockToday)
        val form = new MoveDateFormProvider(dates)()

        val data = formData(validDate)
        val result = form.bind(data)
        result.errors must not be empty
      }
    }


    def formData(date: LocalDate): Map[String, String] = Map(
      "value.day" -> date.getDayOfMonth.toString,
      "value.month" -> date.getMonthValue.toString,
      "value.year" -> date.getYear.toString
    )
  }
}
