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

import base.SpecBase
import config.Constants.{exclusionCodeSixFollowingMonth, exclusionCodeSixTenthOfMonth}
import connectors.VatReturnsConnector
import models.exclusions.ExclusionReason.{CeasedTrade, FailsToComply, NoLongerMeetsConditions, NoLongerSupplies, Reversal, VoluntarilyLeaves}
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.registration.Registration
import models.requests.OptionalDataRequest
import models.{Period, Quarter, StandardPeriod, VatReturn}
import org.mockito.Mockito.when
import org.scalacheck.Gen
import pages.reversals.CancelLeaveSchemeErrorPage
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import utils.FutureSyntax.FutureOps

import java.time.temporal.IsoFields
import java.time.{Clock, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckCancelRequestToLeaveFilterSpec extends SpecBase {

  private val submittedVatReturns: Seq[VatReturn] = Gen.listOfN(4, arbitraryVatReturn.arbitrary).sample.value
  private val mockVatReturnsConnector: VatReturnsConnector = mock[VatReturnsConnector]

  class Harness(clock: Option[Clock] = None) extends CheckCancelRequestToLeaveFilterImpl(clock.getOrElse(stubClockAtArbitraryDate), mockVatReturnsConnector) {
    def callFilter(request: OptionalDataRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val today: LocalDate = LocalDate.now(stubClockAtArbitraryDate)
  private val quarter = Quarter.fromString(s"Q${today.get(IsoFields.QUARTER_OF_YEAR)}").get
  private val effectivePeriod: Period = StandardPeriod(today.getYear, quarter)

  ".filter" - {

    Seq(NoLongerSupplies, VoluntarilyLeaves).foreach { exclusionReason =>

      s"must return None when exclusion reason is code $exclusionReason and today is before the exclusion effective date" in {

        val effectiveDate = effectivePeriod.lastDay.plusDays(1)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, effectivePeriod, effectiveDate)))

        val application = applicationBuilder().build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness()

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      s"must redirect to the Cancel Leave Scheme Error Page when exclusion reason is code $exclusionReason and" +
        s"today is equal to or after the exclusion effective date" in {

        val effectiveDate = today

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, effectivePeriod, effectiveDate)))

        val application = applicationBuilder().build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness()

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(CancelLeaveSchemeErrorPage.route(emptyWaypoints).url)
        }
      }
    }

    Seq(Reversal, CeasedTrade, NoLongerMeetsConditions, FailsToComply).foreach { exclusionReason =>

      s"must redirect to the Cancel Leave Scheme Error Page when exclusion reason code is code $exclusionReason irrespective of date" in {

        val effectiveDate = arbitraryDate.arbitrary.sample.value

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, effectivePeriod, effectiveDate)))

        val application = applicationBuilder().build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness()

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(CancelLeaveSchemeErrorPage.route(emptyWaypoints).url)
        }
      }
    }

    "when exclusion reason code is code 6 - TransferringMSID" - {

      "must return None when the effective date is on the 10th day of the following month they changed country" in {

        when(mockVatReturnsConnector.getSubmittedVatReturns) thenReturn submittedVatReturns.toFuture

        val today: LocalDate = LocalDate.of(2024, 5, exclusionCodeSixTenthOfMonth)
        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock))
          .overrides(bind[VatReturnsConnector].toInstance(mockVatReturnsConnector))
          .build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must return None when the effective date is before the 10th day of the following month they changed country" in {

        val today: LocalDate = LocalDate.of(2024, 5, 9)
        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock)).build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must return None when the effective date is on or before the 10th day of the following month they changed country" +
        "and effective period is equal to the current period (too early to submit return for that period)" in {

        val today: LocalDate = LocalDate.of(2024, 5, 9)
        val quarter = Quarter.fromString(s"Q${today.get(IsoFields.QUARTER_OF_YEAR)}").get
        val effectivePeriod: Period = StandardPeriod(today.getYear, quarter)

        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock)).build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must return None when the effective date is on or before the 10th day of the following month they changed country" +
        "and effective period does not have an associated submitted return " in {

        val today: LocalDate = LocalDate.of(2024, 5, 9)
        val quarter = Quarter.fromString(s"Q${today.minusMonths(3).get(IsoFields.QUARTER_OF_YEAR)}").get
        val effectivePeriod: Period = StandardPeriod(today.getYear, quarter)

        val submittedVatReturnsWithoutEffectivePeriod: Seq[VatReturn] =
          Gen.listOfN(4, arbitraryVatReturn.arbitrary.suchThat(vr => vr.period != effectivePeriod)).sample.value

        when(mockVatReturnsConnector.getSubmittedVatReturns) thenReturn submittedVatReturnsWithoutEffectivePeriod.toFuture

        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock))
          .overrides(bind[VatReturnsConnector].toInstance(mockVatReturnsConnector))
          .build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must redirect to the Cancel Leave Scheme Error Page when the effective date is on or before the 10th day of the following month they changed country" +
        "and effective period has an associated submitted return " in {

        val today: LocalDate = LocalDate.of(2024, 5, 9)
        val quarter = Quarter.fromString(s"Q${today.minusMonths(3).get(IsoFields.QUARTER_OF_YEAR)}").get
        val effectivePeriod: Period = StandardPeriod(today.getYear, quarter)

        val submittedVatReturnsWithEffectivePeriod: Seq[VatReturn] =
          Gen.listOfN(4, arbitraryVatReturn.arbitrary).sample.value ++ Seq(VatReturn(period = effectivePeriod))

        when(mockVatReturnsConnector.getSubmittedVatReturns) thenReturn submittedVatReturnsWithEffectivePeriod.toFuture

        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock))
          .overrides(bind[VatReturnsConnector].toInstance(mockVatReturnsConnector))
          .build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(CancelLeaveSchemeErrorPage.route(emptyWaypoints).url)
        }
      }

      "must redirect to the Cancel Leave Scheme Error Page when the effective date is after the 10th day of the following month they changed country" in {

        val today: LocalDate = LocalDate.of(2024, 5, 11)
        val newClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())
        val effectiveDate = today.minusMonths(exclusionCodeSixFollowingMonth)

        val excludedRegistration: Registration = registration
          .copy(excludedTrader = Some(ExcludedTrader(vrn, ExclusionReason.TransferringMSID, effectivePeriod, effectiveDate)))

        val application = applicationBuilder(clock = Some(newClock)).build()

        running(application) {

          val request = OptionalDataRequest(FakeRequest(), userAnswersId, vrn, excludedRegistration, Some(completeUserAnswers))
          val controller = new Harness(Some(newClock))

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(CancelLeaveSchemeErrorPage.route(emptyWaypoints).url)
        }
      }
    }
  }
}
