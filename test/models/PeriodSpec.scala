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

import base.SpecBase
import models.Quarter.*
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PeriodSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val year = 2024

  ".fromString" - {

    "must resolve for valid periods" in {

      Period.fromString("2021-Q1").get mustEqual StandardPeriod(2021, Q1)

    }

    "must return None for invalid periods" in {

      forAll(arbitrary[String]) {
        string =>

        Period.fromString(string) mustBe None
      }
    }
  }

  "displayText" - {

    "return formatted date range correctly" in {

      implicit val messages: Messages = mock[Messages]
      when(messages("site.to")).thenReturn("to")

      val quarter = Q1
      val firstDay = LocalDate.of(year, quarter.startMonth, 1)
      val lastDay = firstDay.plusMonths(3).minusDays(1)

      val lastDayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      val expectedText = s"${lastDay.format(lastDayFormatter)}"

      val period = Period.fromString(s"$year-Q1")
      period.get.displayText() mustBe expectedText
    }
  }

  "getPreviousPeriod" - {

    val previousYear = 2023

    "return the correct previous quarter" in {
      StandardPeriod(year, Q4).getPreviousPeriod mustBe StandardPeriod(year, Q3)
      StandardPeriod(year, Q3).getPreviousPeriod mustBe StandardPeriod(year, Q2)
      StandardPeriod(year, Q2).getPreviousPeriod mustBe StandardPeriod(year, Q1)
      StandardPeriod(year, Q1).getPreviousPeriod mustBe StandardPeriod(previousYear, Q4)
    }
  }

  "getNextPeriod" - {

    "return the correct next period quater" in {

      val nextYear = 2025

      StandardPeriod(year, Q1).getNextPeriod mustBe StandardPeriod(year, Q2)
      StandardPeriod(year, Q2).getNextPeriod mustBe StandardPeriod(year, Q3)
      StandardPeriod(year, Q3).getNextPeriod mustBe StandardPeriod(year, Q4)
      StandardPeriod(year, Q4).getNextPeriod mustBe StandardPeriod(nextYear, Q1)

    }
  }

  "parse a valid period string" in {

    val periodString = "2023-Q1"

    val period = Period.fromString(periodString)

    period mustBe Some(StandardPeriod(2023, Q1))
  }

  "return None for an invalid period string" in {

    val invalidString = "invalid"

    val period = Period.fromString(invalidString)

    period mustBe None
  }

  "generate the correct sequence of RadioItem instances" in {

    val periods = Seq(
      StandardPeriod(2023, Q1),
      StandardPeriod(2023, Q2),
      StandardPeriod(2023, Q3)
    )

    val radioItems = StandardPeriod.options(periods)()

    radioItems must have size 3

    radioItems.head mustBe RadioItem(
      content = Text(periods.head.displayText()),
      value = Some("2023-Q1"),
      id = Some("value_0")
    )

    radioItems(1) mustBe RadioItem(
      content = Text(periods(1).displayText()),
      value = Some("2023-Q2"),
      id = Some("value_1")
    )

    radioItems(2) mustBe RadioItem(
      content = Text(periods(2).displayText()),
      value = Some("2023-Q3"),
      id = Some("value_2")
    )
  }

  "return an empty sequence if no periods are provided" in {

    val periods = Seq.empty[StandardPeriod]

    val radioItems = StandardPeriod.options(periods)()

    radioItems mustBe empty
  }

  "return Some(StandardPeriod) for a valid string" in {

    val validString = "2023-Q1"

    val result = StandardPeriod.fromString(validString)

    result mustBe Some(StandardPeriod(2023, Q1))
  }

  "return None for an invalid string" in {

    val invalidString = "invalid"

    val result = StandardPeriod.fromString(invalidString)

    result mustBe None
  }

  "return None for a string with an invalid format" in {

    val invalidFormatString = "2023-01"

    val result = StandardPeriod.fromString(invalidFormatString)

    result mustBe None
  }

  "return None for a string with an out-of-range month" in {

    val outOfRangeMonthString = "2023-Q7"

    val result = StandardPeriod.fromString(outOfRangeMonthString)

    result mustBe None
  }

}
