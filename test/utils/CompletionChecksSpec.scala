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
import models.requests.DataRequest
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{EuVatNumberPage, MoveCountryPage, MoveDatePage}
import play.api.mvc.AnyContent
import play.api.test.Helpers.running
import controllers.routes
import play.api.mvc.Results.Redirect

class CompletionChecksSpec extends SpecBase with MockitoSugar {

  object TestCompletionChecks extends CompletionChecks

  implicit val request: DataRequest[AnyContent] = mock[DataRequest[AnyContent]]

  "CompletionChecks" - {

    "validate" - {

      "should return true if all validations pass" in {

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers)).build()

        running(application) {
          when(request.userAnswers).thenReturn(completeUserAnswers)
        }

        TestCompletionChecks.validate() mustBe true

      }

      "should return false if any validation fails" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          when(request.userAnswers).thenReturn(emptyUserAnswers)
        }

        TestCompletionChecks.validate() mustBe false
      }

    }

    "getFirstValidationErrorRedirect" - {

      "return the first incomplete redirect" in {

        val incompleteAnswers = emptyUserAnswers
          .set(MoveCountryPage, true).success.value
          .set(MoveDatePage, moveDate).success.value
          .set(EuVatNumberPage, taxNumber).success.value

        val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

        running(application) {
          when(request.userAnswers).thenReturn(incompleteAnswers)
          val result = TestCompletionChecks.getFirstValidationErrorRedirect(emptyWaypoints)

          result mustBe Some(Redirect(routes.EuCountryController.onPageLoad(emptyWaypoints)))
        }
      }

      "return None if no validation errors" in {

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers)).build()

        running(application) {
          when(request.userAnswers).thenReturn(completeUserAnswers)
          val result = TestCompletionChecks.getFirstValidationErrorRedirect(emptyWaypoints)

          result mustBe None
        }
      }
    }
  }
}
