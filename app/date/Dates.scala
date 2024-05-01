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

import java.time.{Clock, LocalDate, ZoneOffset}
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class Dates @Inject() (val today: Today) {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  private val digitsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MM yyyy")

  val dateHint: String = digitsFormatter.format(today.date)
  val lastDayOfQuarterFormatted: String = formatter.format(lastDayOfQuarter)

  def getLeaveDateWhenStoppedUsingService(exclusionDate: LocalDate): LocalDate = {
    val exclusionMonth = exclusionDate.getMonth
    val quarter = Quarter.values.find(q => exclusionMonth.compareTo(q.startMonth) >= 0 && exclusionMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.endMonth.getValue).withDayOfMonth(q.endMonth.maxLength())
      case None    => throw new IllegalStateException("No quarter found for the current month")
    }

  }
  def getLeaveDateWhenStoppedSellingGoods(leaveDate: LocalDate): LocalDate = {
    val leaveMonth = leaveDate.getMonth
    val quarter = Quarter.values.find(q => leaveMonth.compareTo(q.startMonth) >= 0 && leaveMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.endMonth.getValue).withDayOfMonth(q.endMonth.maxLength())
      case None    => throw new IllegalStateException("No quarter found for the current month")
    }
  }

  def firstDayOfQuarter: LocalDate = {
    val currentMonth = today.date.getMonth
    val quarter = Quarter.values.find(q => currentMonth.compareTo(q.startMonth) >= 0 && currentMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.startMonth.getValue).withDayOfMonth(1)
      case None    => throw new IllegalStateException("No quarter found for the current month")
    }
  }

  def lastDayOfQuarter: LocalDate = {
    val currentMonth = today.date.getMonth
    val quarter = Quarter.values.find(q => currentMonth.compareTo(q.startMonth) >= 0 && currentMonth.compareTo(q.endMonth) <= 0)
    quarter match {
      case Some(q) => today.date.withMonth(q.endMonth.getValue).withDayOfMonth(q.endMonth.maxLength())
      case None    => throw new IllegalStateException("No quarter found for the current month")
    }
  }

}

object Dates {
  val clock: Clock = Clock.systemDefaultZone.withZone(ZoneOffset.UTC)
}
