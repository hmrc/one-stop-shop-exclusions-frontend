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

package models.exclusions

import logging.Logging
import models.Period
import models.Period.getPeriod
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

case class ExcludedTrader(
                           vrn: Vrn,
                           exclusionReason: ExclusionReason,
                           effectiveDate: LocalDate
                         ) {

  val finalReturnPeriod: Period = {
    if (exclusionReason == ExclusionReason.TransferringMSID) {
      getPeriod(effectiveDate)
    } else {
      getPeriod(effectiveDate).getPreviousPeriod
    }
  }
}


object ExcludedTrader extends Logging {

  implicit val format: OFormat[ExcludedTrader] = Json.format[ExcludedTrader]
}


