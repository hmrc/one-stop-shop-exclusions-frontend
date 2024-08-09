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

package utils

import base.SpecBase
import models.UserAnswers
import models.requests.DataRequest
import org.mockito.Mockito.when
import pages._
import play.api.mvc.AnyContent
import play.api.mvc.Results.Redirect
import play.api.test.Helpers._

class CompletionChecksSpec extends SpecBase {

  private implicit val request: DataRequest[AnyContent] = mock[DataRequest[AnyContent]]

  private val validUserAnswers: UserAnswers = emptyUserAnswers
    .set(MoveCountryPage, true).success.value
    .set(EuCountryPage, country).success.value
    .set(MoveDatePage, moveDate).success.value
    .set(EuVatNumberPage, taxNumber).success.value

  object CompletionChecks extends CompletionChecks

  "CompletionChecks" - {

    ".validate" - {

      "when user answers Yes to has moved country" - {

        "must return true when userAnswers are valid" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn validUserAnswers

            val result = CompletionChecks.validate()

            result mustBe true
          }
        }

        "must return false when userAnswers are invalid" in {

          val updatedAnswers: UserAnswers = validUserAnswers.remove(EuVatNumberPage).success.value

          val application = applicationBuilder(userAnswers = Some(updatedAnswers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn updatedAnswers

            val result = CompletionChecks.validate()

            result mustBe false
          }
        }
      }

      "when user answers Yes to stopped selling goods" - {

        val answers = emptyUserAnswers
          .set(MoveCountryPage, false).success.value
          .set(StopSellingGoodsPage, true).success.value
          .set(StoppedSellingGoodsDatePage, moveDate).success.value

        "must return true when userAnswers are valid" in {

          val application = applicationBuilder(userAnswers = Some(answers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn answers

            val result = CompletionChecks.validate()

            result mustBe true
          }
        }

        "must return false when userAnswers are invalid" in {

          val updatedAnswers: UserAnswers = answers.remove(StoppedSellingGoodsDatePage).success.value

          val application = applicationBuilder(userAnswers = Some(updatedAnswers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn updatedAnswers

            val result = CompletionChecks.validate()

            result mustBe false
          }
        }
      }

      "when user answers Yes to leave service" - {

        val answers = emptyUserAnswers
          .set(MoveCountryPage, false).success.value
          .set(StopSellingGoodsPage, false).success.value
          .set(LeaveSchemePage, true).success.value
          .set(StoppedUsingServiceDatePage, moveDate).success.value

        "must return true when userAnswers are valid" in {

          val application = applicationBuilder(userAnswers = Some(answers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn answers

            val result = CompletionChecks.validate()

            result mustBe true
          }
        }

        "must return false when userAnswers are invalid" in {

          val updatedAnswers: UserAnswers = answers.remove(StoppedUsingServiceDatePage).success.value

          val application = applicationBuilder(userAnswers = Some(updatedAnswers))
            .build()

          running(application) {
            when(request.userAnswers) thenReturn updatedAnswers

            val result = CompletionChecks.validate()

            result mustBe false
          }
        }
      }
    }

    ".getFirstValidationErrorRedirect" - {

      "must return redirect on first validation error" in {

        val invalidUserAnswers = validUserAnswers.remove(EuVatNumberPage).success.value

        val application = applicationBuilder(userAnswers = Some(invalidUserAnswers))
          .build()

        running(application) {

          when(request.userAnswers) thenReturn invalidUserAnswers
          val result = CompletionChecks.getFirstValidationErrorRedirect(EmptyWaypoints)

          result mustBe Some(Redirect(controllers.routes.EuVatNumberController.onPageLoad(EmptyWaypoints)))
        }
      }

      "must return None when there are no validation errors" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers))
          .build()

        running(application) {

          when(request.userAnswers) thenReturn validUserAnswers
          val result = CompletionChecks.getFirstValidationErrorRedirect(EmptyWaypoints)

          result mustBe None
        }
      }

      "must redirect to Move Country Page when user hasn't completed any journey" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {

          when(request.userAnswers) thenReturn emptyUserAnswers
          val result = CompletionChecks.getFirstValidationErrorRedirect(EmptyWaypoints)

          result mustBe Some(Redirect(controllers.routes.MoveCountryController.onPageLoad(EmptyWaypoints)))
        }
      }
    }
  }
}
