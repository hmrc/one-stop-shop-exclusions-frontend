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

package date

import models.Quarter
import models.Quarter.Q4
import models.requests.DataRequest

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneOffset}
import javax.inject.Inject

class Dates @Inject()(val today: Today) {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val MoveDayOfMonthSplit: Int = 10
  private val StopDayOfMonthSplit: Int = 15

  private val digitsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")

  val dateHint: String = digitsFormatter.format(today.date)
  val lastDayOfQuarterFormatted: String = formatter.format(lastDayOfQuarter)
  val firstDayOfNextQuarterFormatted: String = formatter.format(firstDayOfNextQuarter)

  def minMoveDate: LocalDate =
    (if (today.date.getDayOfMonth <= MoveDayOfMonthSplit) today.date.minusMonths(1) else today.date)
      .withDayOfMonth(1)

  def maxMoveDate: LocalDate =
    today.date.plusMonths(1).withDayOfMonth(MoveDayOfMonthSplit)

  def getLeaveDateWhenStoppedUsingService(exclusionDate: LocalDate): LocalDate = {
    val lastDayOfTheQuarter = today.date.`with`(lastDayOfQuarter)
    val firstDayOfTheNextQuarter = today.date.`with`(firstDayOfNextQuarter)

    if (today.date <= lastDayOfTheQuarter.minusDays(StopDayOfMonthSplit)) {
      firstDayOfTheNextQuarter
    } else {
      firstDayOfTheNextQuarter.plusMonths(3)
    }
  }

  def getLeaveDateWhenStoppedSellingGoods: LocalDate = {
    today.date.`with`(firstDayOfNextQuarter)
  }

  def firstDayOfQuarter: LocalDate = {
    val currentMonth = today.date.getMonth
    val quarter = Quarter.values.find(q => currentMonth.compareTo(q.startMonth) >= 0 && currentMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.startMonth.getValue).withDayOfMonth(1)
      case None => throw new IllegalStateException("No quarter found for the current month")
    }
  }

  def lastDayOfQuarter: LocalDate = {
    val currentMonth = today.date.getMonth
    val quarter = Quarter.values.find(q => currentMonth.compareTo(q.startMonth) >= 0 && currentMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.endMonth.getValue).withDayOfMonth(q.endMonth.maxLength())
      case None => throw new IllegalStateException("No quarter found for the current month")
    }
  }

  def firstDayOfNextQuarter: LocalDate = {
    val currentMonth = today.date.getMonth
    val currentQuarter = Quarter.values.find(q => currentMonth.compareTo(q.startMonth) >= 0 && currentMonth.compareTo(q.endMonth) <= 0)

    currentQuarter match {
      case Some(q) =>
        val nextQuarterIndex = (Quarter.values.indexOf(q) + 1) % Quarter.values.size
        val nextQuarter = Quarter.values(nextQuarterIndex)
        val year = if (currentQuarter.contains(Q4)) {
          today.date.getYear + 1
        } else {
          today.date.getYear
        }
        LocalDate.of(year, nextQuarter.startMonth, 1)
      case None =>
        throw new IllegalStateException("No quarter found for the current month")
    }
  }

  def getMinimumDateBasedOnCommencementDate()(implicit request: DataRequest[_]): LocalDate = {
    if (LocalDate.now(Dates.clock).isBefore(request.registration.commencementDate)) {
      request.registration.commencementDate.minusMonths(3)
    } else {
      request.registration.commencementDate
    }
  }

}

object Dates {
  val clock: Clock = Clock.systemDefaultZone.withZone(ZoneOffset.UTC)
}
