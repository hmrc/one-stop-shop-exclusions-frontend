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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import date.{Dates, Today}
import models.{CheckMode, Country}
import models.audit.{ExclusionAuditModel, ExclusionAuditType, SubmissionResult}
import models.exclusions.ExclusionReason
import models.responses.UnexpectedResponseStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import viewmodels.checkAnswers.{EuCountrySummary, EuVatNumberSummary, MoveCountrySummary, MoveDateSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockAuditService: AuditService = mock[AuditService]
  private val mockRegistrationConnector = mock[RegistrationConnector]

  private val today: LocalDate = LocalDate.now
  private val mockToday: Today = mock[Today]
  when(mockToday.date).thenReturn(today)

  override protected def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
    reset(mockAuditService)
  }

  private val date: Dates = new Dates(mockToday)
  private val answers = emptyUserAnswers
    .set(MoveCountryPage, true).success.value
    .set(EuCountryPage, Country("DE", "Germany")).success.value
    .set(MoveDatePage, today).success.value
    .set(EuVatNumberPage, "DE123456789").success.value

  "Check Your Answers Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {

          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val view = application.injector.instanceOf[CheckYourAnswersView]
          val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
          val list = SummaryListViewModel(
            Seq(
              MoveCountrySummary.row(answers, waypoints, CheckYourAnswersPage),
              EuCountrySummary.countryRow(answers, waypoints, CheckYourAnswersPage),
              MoveDateSummary.rowMoveDate(answers, waypoints, CheckYourAnswersPage, date),
              EuVatNumberSummary.rowEuVatNumber(answers, waypoints, CheckYourAnswersPage)
            ).flatten
          )

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(waypoints, list, isValid = true, appConfig.ossYourAccountUrl)(request, messages(application)).toString
        }
      }
    }

    ".onSubmit" - {

      "must redirect to the correct page when the validation passes" in {

        when(mockRegistrationConnector.amend(any())(any())) thenReturn
          Future.successful(Right(()))

        val userAnswers = completeUserAnswers
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = false).url)

          val result = route(application, request).value

          val expectedAuditEvent = ExclusionAuditModel(
            ExclusionAuditType.ExclusionRequestSubmitted,
            userAnswersId,
            "",
            vrn.vrn,
            userAnswers.toUserAnswersForAudit,
            registration,
            Some(ExclusionReason.TransferringMSID),
            SubmissionResult.Success
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe ApplicationCompletePage.route(emptyWaypoints).url
          verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
        }
      }

      "must redirect to the failure page and audit a failure event when the validation passes but amend call failures" in {

        when(mockRegistrationConnector.amend(any())(any())) thenReturn
          Future.successful(Left(UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "Error occurred")))

        val userAnswers = completeUserAnswers
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(emptyWaypoints, incompletePrompt = false).url)

          val result = route(application, request).value

          val expectedAuditEvent = ExclusionAuditModel(
            ExclusionAuditType.ExclusionRequestSubmitted,
            userAnswersId,
            "",
            vrn.vrn,
            userAnswers.toUserAnswersForAudit,
            registration,
            Some(ExclusionReason.TransferringMSID),
            SubmissionResult.Failure
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.SubmissionFailureController.onPageLoad().url
          verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
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
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}