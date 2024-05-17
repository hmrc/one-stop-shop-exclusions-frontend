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

package controllers

import base.SpecBase
import connectors.VatReturnsConnector
import forms.CancelLeaveSchemeRequestFormProvider
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.registration.Registration
import models.requests.OptionalDataRequest
import models.{Period, UserAnswers, VatReturn}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.{CancelLeaveSchemeRequestPage, JourneyRecoveryPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.FutureSyntax.FutureOps
import views.html.CancelLeaveSchemeRequestView

import java.time.{Clock, LocalDate, ZoneId}

class CancelLeaveSchemeRequestControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new CancelLeaveSchemeRequestFormProvider()
  private val form: Form[Boolean] = formProvider()
  private val period: Period = arbitraryStandardPeriod.arbitrary.sample.value

  private val mockVatReturnsConnector: VatReturnsConnector = mock[VatReturnsConnector]

  private def excludedRegistration(exclusionReason: ExclusionReason, effectiveDate: LocalDate): Registration = registration.copy(
    excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, period, effectiveDate))
  )

  private lazy val cancelLeaveSchemeRequestRoute: String = routes.CancelLeaveSchemeRequestController.onPageLoad(emptyWaypoints).url

  "CancelLeaveSchemeRequest Controller" - {

    "when trader is excluded with code 5 and today is before the exclusion effective date" - {

      "must return OK and the correct view for a GET" in {

        val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
        val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

        val application = applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          maybeRegistration = Some(excludedRegistrationCode5)
        ).build()

        running(application) {

          val request = OptionalDataRequest(
            FakeRequest(GET, cancelLeaveSchemeRequestRoute),
            userAnswersId,
            vrn,
            excludedRegistrationCode5,
            Some(emptyUserAnswers)
          )

          val result = route(application, request).value
          val view = application.injector.instanceOf[CancelLeaveSchemeRequestView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, emptyWaypoints)(request, messages(application)).toString
        }
      }
    }

    "when trader is excluded with code 6 and today is before the tenth day of the following month of the exclusion effective date" - {

      "must return OK and the correct view for a GET " in {

        val submittedVatReturns: Seq[VatReturn] = Gen.listOfN(4, arbitraryVatReturn.arbitrary).sample.value
        when(mockVatReturnsConnector.getSubmittedVatReturns) thenReturn submittedVatReturns.toFuture

        val today: LocalDate = LocalDate.of(2024, 5, 10)
        val clock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

        val effectiveDate: LocalDate = today.minusMonths(1)
        val excludedRegistrationCode6 = excludedRegistration(ExclusionReason.TransferringMSID, effectiveDate)

        val application = applicationBuilder(
          clock = Some(clock),
          userAnswers = Some(emptyUserAnswers),
          maybeRegistration = Some(excludedRegistrationCode6)
        )
          .overrides(bind[VatReturnsConnector].toInstance(mockVatReturnsConnector))
          .build()

        running(application) {

          val request = OptionalDataRequest(
            FakeRequest(GET, cancelLeaveSchemeRequestRoute),
            userAnswersId,
            vrn,
            excludedRegistrationCode6,
            Some(emptyUserAnswers)
          )

          val result = route(application, request).value
          val view = application.injector.instanceOf[CancelLeaveSchemeRequestView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, emptyWaypoints)(request, messages(application)).toString
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(CancelLeaveSchemeRequestPage, true).success.value

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        maybeRegistration = Some(excludedRegistrationCode5)
      ).build()

      running(application) {

        val request = OptionalDataRequest(
          FakeRequest(GET, cancelLeaveSchemeRequestRoute),
          userAnswersId,
          vrn,
          excludedRegistrationCode5,
          Some(userAnswers)
        )

        val view = application.injector.instanceOf[CancelLeaveSchemeRequestView]

        val result = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe view(form.fill(true), emptyWaypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        maybeRegistration = Some(excludedRegistrationCode5)
      ).overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {

        val request = OptionalDataRequest(
          FakeRequest(POST, cancelLeaveSchemeRequestRoute).withFormUrlEncodedBody(("value", "true")),
          userAnswersId,
          vrn,
          excludedRegistrationCode5,
          Some(emptyUserAnswers)
        )

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(CancelLeaveSchemeRequestPage, true).success.value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe CancelLeaveSchemeRequestPage.navigate(emptyWaypoints, emptyUserAnswers, expectedAnswers).route.url
        verify(mockSessionRepository, times(1)).set(refEq(expectedAnswers, "lastUpdated"))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        maybeRegistration = Some(excludedRegistrationCode5)
      ).build()

      running(application) {

        val request = OptionalDataRequest(
          FakeRequest(POST, cancelLeaveSchemeRequestRoute),
          userAnswersId,
          vrn,
          excludedRegistrationCode5,
          Some(emptyUserAnswers)
        )

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CancelLeaveSchemeRequestView]

        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe view(boundForm, emptyWaypoints)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cancelLeaveSchemeRequestRoute)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe JourneyRecoveryPage.route(emptyWaypoints).url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, cancelLeaveSchemeRequestRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe JourneyRecoveryPage.route(emptyWaypoints).url
      }
    }
  }
}
