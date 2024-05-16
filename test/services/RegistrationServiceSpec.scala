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

package services

import base.SpecBase
import connectors.RegistrationConnector
import data.RegistrationData
import models.audit.ExclusionAuditType
import models.exclusions.EtmpExclusionReason
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.{StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global


class RegistrationServiceSpec extends SpecBase with BeforeAndAfterEach with RegistrationData {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockAuditService: AuditService = mock[AuditService]
  private val registrationService = new RegistrationService(stubClock, mockRegistrationConnector, mockAuditService)

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
    reset(mockAuditService)
  }

  private def buildRegistration() = {
    registrationRequest.copy(
      vrn = vrn,
      registeredCompanyName = registration.registeredCompanyName,
      tradingNames = registration.tradingNames,
      vatDetails = registration.vatDetails,
      euRegistrations = registration.euRegistrations,
      contactDetails = registration.contactDetails,
      websites = registration.websites,
      commencementDate = registration.commencementDate,
      previousRegistrations = registration.previousRegistrations,
      bankDetails = registration.bankDetails,
      isOnlineMarketplace = registration.isOnlineMarketplace,
      niPresence = registration.niPresence,
      dateOfFirstSale = registration.dateOfFirstSale,
      nonCompliantReturns = registration.nonCompliantReturns,
      nonCompliantPayments = registration.nonCompliantPayments,
      submissionReceived = registration.submissionReceived
    )
  }

  ".amendRegistration" - {

    "when transferring of MSID" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val expectedAmendRegistrationRequest = buildRegistration()

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            completeUserAnswers,
            registration,
            Some(EtmpExclusionReason.TransferringMSID),
            ExclusionAuditType.ExclusionRequestSubmitted
          ).futureValue mustBe Right(())
          verify(mockRegistrationConnector, times(1)).amend(eqTo(expectedAmendRegistrationRequest))(any())
        }
      }

    }

    "when no longer supplying goods" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val stoppedSellingGoodsDate = LocalDate.of(2023, 10, 5)
        val userAnswers = emptyUserAnswers
          .set(StoppedSellingGoodsDatePage, stoppedSellingGoodsDate).success.value
        val expectedAmendRegistrationRequest = buildRegistration()

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            userAnswers,
            registration,
            Some(EtmpExclusionReason.NoLongerSupplies),
            ExclusionAuditType.ExclusionRequestSubmitted
          ).futureValue mustBe Right(())
          verify(mockRegistrationConnector, times(1)).amend(eqTo(expectedAmendRegistrationRequest))(any())
        }

      }

    }

    "when voluntarily leaves" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val stoppedUsingServiceDate = LocalDate.of(2023, 10, 4)

        val userAnswers = emptyUserAnswers
          .set(StoppedUsingServiceDatePage, stoppedUsingServiceDate).success.value

        val exceptedAmendRegistrationRequest = buildRegistration()

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            userAnswers,
            registration,
            Some(EtmpExclusionReason.VoluntarilyLeaves),
            ExclusionAuditType.ExclusionRequestSubmitted
          ).futureValue mustBe Right(())
          verify(mockRegistrationConnector, times(1)).amend(eqTo(exceptedAmendRegistrationRequest))(any())
        }

      }

    }
  }

}
