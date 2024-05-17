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

import java.time.{LocalDate, ZoneOffset}
import base.SpecBase
import connectors.RegistrationConnector
import date.{Dates, Today, TodayImpl}
import forms.StoppedSellingGoodsDateFormProvider
import models.UserAnswers
import models.audit.{ExclusionAuditModel, ExclusionAuditType, SubmissionResult}
import models.exclusions.ExclusionReason
import models.responses.UnexpectedResponseStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.StoppedSellingGoodsDatePage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import views.html.StoppedSellingGoodsDateView

import scala.concurrent.Future


class StoppedSellingGoodsDateControllerSpec extends SpecBase with BeforeAndAfterEach {

  val today: Today = new TodayImpl(Dates.clock)
  val dates = new Dates(today)

  private val formProvider = new StoppedSellingGoodsDateFormProvider(dates)
  private def form(currentDate: LocalDate = LocalDate.now(), registrationDate: LocalDate = LocalDate.now()): Form[LocalDate] =
    formProvider.apply(currentDate, registrationDate)

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  lazy val stoppedSellingGoodsDateRoute: String = routes.StoppedSellingGoodsDateController.onPageLoad(emptyWaypoints).url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockAuditService = mock[AuditService]

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, stoppedSellingGoodsDateRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, stoppedSellingGoodsDateRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "StoppedSellingGoodsDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[StoppedSellingGoodsDateView]

        val dates = application.injector.instanceOf[Dates]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form(),
          emptyWaypoints,
          dates.lastDayOfQuarterFormatted,
          dates.dateHint
        )(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(StoppedSellingGoodsDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[StoppedSellingGoodsDateView]

        val dates = application.injector.instanceOf[Dates]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form().fill(validAnswer),
          emptyWaypoints,
          dates.lastDayOfQuarterFormatted,
          dates.dateHint
        )(getRequest(), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockRegistrationConnector.amend(any())(any())) thenReturn Future.successful(Right(()))

      val userAnswers = UserAnswers(userAnswersId).set(StoppedSellingGoodsDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[RegistrationConnector].toInstance(mockRegistrationConnector),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val result = route(application, postRequest()).value

        val expectedAuditEvent = ExclusionAuditModel(
          ExclusionAuditType.ExclusionRequestSubmitted,
          userAnswersId,
          "",
          vrn.vrn,
          userAnswers.toUserAnswersForAudit,
          registration,
          Some(ExclusionReason.NoLongerSupplies),
          SubmissionResult.Success
        )

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual StoppedSellingGoodsDatePage.navigate(emptyWaypoints, emptyUserAnswers, userAnswers).url
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }

    "must redirect to the failure page when valid data is submitted but api returns failure" in {

      when(mockRegistrationConnector.amend(any())(any())) thenReturn
        Future.successful(Left(UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "Error occurred")))

      val userAnswers = UserAnswers(userAnswersId).set(StoppedSellingGoodsDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[RegistrationConnector].toInstance(mockRegistrationConnector),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val result = route(application, postRequest()).value

        val expectedAuditEvent = ExclusionAuditModel(
          ExclusionAuditType.ExclusionRequestSubmitted,
          userAnswersId,
          "",
          vrn.vrn,
          userAnswers.toUserAnswersForAudit,
          registration,
          Some(ExclusionReason.NoLongerSupplies),
          SubmissionResult.Failure
        )

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SubmissionFailureController.onPageLoad().url
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, stoppedSellingGoodsDateRoute).withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[StoppedSellingGoodsDateView]

        val dates = application.injector.instanceOf[Dates]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          emptyWaypoints,
          dates.lastDayOfQuarterFormatted,
          dates.dateHint
        )(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
