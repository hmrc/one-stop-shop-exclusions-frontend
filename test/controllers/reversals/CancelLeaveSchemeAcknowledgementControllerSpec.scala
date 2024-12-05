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

package controllers.reversals

import base.SpecBase
import config.FrontendAppConfig
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.registration.Registration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.reversals.CancelLeaveSchemeAcknowledgementView
import play.api.inject.bind

import java.time.LocalDate
import scala.concurrent.Future

class CancelLeaveSchemeAcknowledgementControllerSpec extends SpecBase with BeforeAndAfterEach {

  private def excludedRegistration(exclusionReason: ExclusionReason, effectiveDate: LocalDate): Registration = registration.copy(
    excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, effectiveDate))
  )

  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "CancelLeaveSchemeAcknowledgement Controller" - {

    "must return OK and the correct view for a GET and clear the session when UserAnswers is present" in {

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        maybeRegistration = Some(excludedRegistrationCode5)
      ).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.CancelLeaveSchemeAcknowledgementController.onPageLoad().url)

        val result = route(application, request).value

        val config = application.injector.instanceOf[FrontendAppConfig]

        val view = application.injector.instanceOf[CancelLeaveSchemeAcknowledgementView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(config.ossYourAccountUrl)(request, messages(application)).toString

        verify(mockSessionRepository).clear(emptyUserAnswers.id)
      }
    }

    "must return OK and the correct view for a GET" in {

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        maybeRegistration = Some(excludedRegistrationCode5)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.CancelLeaveSchemeAcknowledgementController.onPageLoad().url)

        val result = route(application, request).value

        val config = application.injector.instanceOf[FrontendAppConfig]

        val view = application.injector.instanceOf[CancelLeaveSchemeAcknowledgementView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(config.ossYourAccountUrl)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when UserAnswers is None" in {

      val effectiveDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate).plusDays(1)
      val excludedRegistrationCode5 = excludedRegistration(ExclusionReason.VoluntarilyLeaves, effectiveDate)

      val application = applicationBuilder(
        userAnswers = None,
        maybeRegistration = Some(excludedRegistrationCode5)
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.CancelLeaveSchemeAcknowledgementController.onPageLoad().url)

        val result = route(application, request).value

        val config = application.injector.instanceOf[FrontendAppConfig]

        val view = application.injector.instanceOf[CancelLeaveSchemeAcknowledgementView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(config.ossYourAccountUrl)(request, messages(application)).toString

        verifyNoInteractions(mockSessionRepository)
      }
    }
  }
}
