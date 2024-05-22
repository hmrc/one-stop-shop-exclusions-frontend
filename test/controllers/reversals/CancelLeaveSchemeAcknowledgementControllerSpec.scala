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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.reversals.CancelLeaveSchemeAcknowledgementView

import java.time.LocalDate

class CancelLeaveSchemeAcknowledgementControllerSpec extends SpecBase {

  private def excludedRegistration(exclusionReason: ExclusionReason, effectiveDate: LocalDate): Registration = registration.copy(
    excludedTrader = Some(ExcludedTrader(vrn, exclusionReason, effectiveDate))
  )

  "CancelLeaveSchemeAcknowledgement Controller" - {

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
  }
}
