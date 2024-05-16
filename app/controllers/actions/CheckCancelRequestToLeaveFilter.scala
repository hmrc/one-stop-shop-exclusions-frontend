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
import logging.Logging
import models.Period
import models.exclusions.ExclusionReason.{NoLongerSupplies, TransferringMSID, VoluntarilyLeaves}
import models.exclusions.ExcludedTrader
import models.requests.OptionalDataRequest
import pages.{EmptyWaypoints, JourneyRecoveryPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TOOO - Rename to CheckCancelRequestToLeaveFilter
class CheckCancelRequestToLeaveFilterImpl @Inject()(clock: Clock)(implicit val executionContext: ExecutionContext)
  extends CheckCancelRequestToLeaveFilter with Logging {

  private val today: LocalDate = LocalDate.now(clock)

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {

    val maybeExclusion: Option[ExcludedTrader] = request.registration.excludedTrader

    maybeExclusion match {
      case Some(excludedTrader) if TransferringMSID == excludedTrader.exclusionReason &&
        isEqualToOrBeforeTenthOfFollowingMonth(excludedTrader.effectiveDate) =>

        // Check returns period here. Check if effective date is within current period, then can't have been submitted and won't
        // need to check returns. If effective is in previous period then call returns to check if return is submitted.
        None.toFuture

      case Some(excludedTrader) if Seq(NoLongerSupplies, VoluntarilyLeaves).contains(excludedTrader.exclusionReason) &&
        LocalDate.now(clock).isBefore(excludedTrader.effectiveDate) =>
        None.toFuture

      case _ => // TODO -> Redirect to Api failure error page
        Some(Redirect(JourneyRecoveryPage.route(EmptyWaypoints).url)).toFuture
    }
  }

  private def isEqualToOrBeforeTenthOfFollowingMonth(effectiveDate: LocalDate): Boolean = {
    val tenthOfFollowingMonth = effectiveDate
      .plusMonths(exclusionCodeSixFollowingMonth)
      .withDayOfMonth(exclusionCodeSixTenthOfMonth)
    today.isBefore(tenthOfFollowingMonth) || today.isEqual(tenthOfFollowingMonth)
  }
}

trait CheckCancelRequestToLeaveFilter extends ActionFilter[OptionalDataRequest]