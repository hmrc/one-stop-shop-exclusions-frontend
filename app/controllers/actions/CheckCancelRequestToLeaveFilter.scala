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

package controllers.actions

import config.Constants.{exclusionCodeSixFollowingMonth, exclusionCodeSixTenthOfMonth}
import connectors.VatReturnsConnector
import logging.Logging
import models.Quarter.{Q1, Q2, Q3, Q4}
import models.exclusions.ExcludedTrader
import models.exclusions.ExclusionReason.{NoLongerSupplies, TransferringMSID, VoluntarilyLeaves}
import models.requests.OptionalDataRequest
import models.{Period, StandardPeriod}
import pages.{EmptyWaypoints, JourneyRecoveryPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate, Month}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckCancelRequestToLeaveFilterImpl @Inject()(
                                                     clock: Clock,
                                                     vatReturnsConnector: VatReturnsConnector
                                                   )(implicit val executionContext: ExecutionContext)
  extends CheckCancelRequestToLeaveFilter with Logging {

  private val today: LocalDate = LocalDate.now(clock)

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {

    val maybeExclusion: Option[ExcludedTrader] = request.registration.excludedTrader

    maybeExclusion match {
      case Some(excludedTrader) if TransferringMSID == excludedTrader.exclusionReason &&
        isEqualToOrBeforeTenthOfFollowingMonth(excludedTrader.effectiveDate) =>

        val currentPeriod: Period = getPeriod(LocalDate.now(clock))

        if (excludedTrader.effectivePeriod == currentPeriod) {
          None.toFuture
        } else {
          checkVatReturnSubmissionStatus(excludedTrader)
        }

      case Some(excludedTrader) if Seq(NoLongerSupplies, VoluntarilyLeaves).contains(excludedTrader.exclusionReason) &&
        LocalDate.now(clock).isBefore(excludedTrader.effectiveDate) =>
        None.toFuture

      case _ => // TODO -> Redirect to Api failure error page
        Some(Redirect(JourneyRecoveryPage.route(EmptyWaypoints).url)).toFuture
    }
  }

  private def checkVatReturnSubmissionStatus(excludedTrader: ExcludedTrader): Future[Option[Result]] = {
    vatReturnsConnector.getSubmittedVatReturns.map { vatReturns =>
      val periods = vatReturns.map(_.period)

      if (periods.contains(excludedTrader.effectivePeriod)) {
        // TODO -> Redirect to correct error page......???
        Some(Redirect(JourneyRecoveryPage.route(EmptyWaypoints).url))
      } else {
        None
      }
    }
  }

  private def isEqualToOrBeforeTenthOfFollowingMonth(effectiveDate: LocalDate): Boolean = {
    val tenthOfFollowingMonth = effectiveDate
      .plusMonths(exclusionCodeSixFollowingMonth)
      .withDayOfMonth(exclusionCodeSixTenthOfMonth)
    today.isBefore(tenthOfFollowingMonth) || today.isEqual(tenthOfFollowingMonth)
  }

  private def getPeriod(effectiveDate: LocalDate): Period = {
    effectiveDate.getMonth match {
      case Month.JANUARY | Month.FEBRUARY | Month.MARCH => StandardPeriod(effectiveDate.getYear, Q1)
      case Month.APRIL | Month.MAY | Month.JUNE => StandardPeriod(effectiveDate.getYear, Q2)
      case Month.JULY | Month.AUGUST | Month.SEPTEMBER => StandardPeriod(effectiveDate.getYear, Q3)
      case Month.OCTOBER | Month.NOVEMBER | Month.DECEMBER => StandardPeriod(effectiveDate.getYear, Q4)
    }
  }
}

trait CheckCancelRequestToLeaveFilter extends ActionFilter[OptionalDataRequest]