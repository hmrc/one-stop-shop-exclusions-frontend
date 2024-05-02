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
import models.CheckMode
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]
          val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
          val list = SummaryListViewModel(Seq.empty)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints, list, isValid = false)(request, messages(application)).toString
        }
      }
    }

    ".onSubmit" - {

      "must redirect to the correct page when the validation passes" in {

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = false).url)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe ApplicationCompletePage.route(emptyWaypoints).url
        }
      }

      "when the user has not answered all necessary data" - {
        "the user is redirected when the incomplete prompt is shown" - {
          "to the Eu Country page when the EU country is missing" in {
            val answers = completeUserAnswers.remove(EuCountryPage).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = true).url)
              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe controllers.routes.EuCountryController.onPageLoad(emptyWaypoints).url
            }
          }

          "to the Move Date page when the move date is missing" in {
            val answers = completeUserAnswers.remove(MoveDatePage).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = true).url)
              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe controllers.routes.MoveDateController.onPageLoad(emptyWaypoints).url
            }
          }

          "to the EU VAT Number page when the tax number is missing" in {
            val answers = completeUserAnswers.remove(EuVatNumberPage).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = true).url)
              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe controllers.routes.EuVatNumberController.onPageLoad(emptyWaypoints).url
            }
          }
        }
      }


      "must redirect to Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}