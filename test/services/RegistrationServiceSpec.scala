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
import models.amend.EtmpExclusionDetails
import models.audit.ExclusionAuditType
import models.exclusions.ExclusionReason
import models.requests.AmendRegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.{StoppedSellingGoodsDatePage, StoppedUsingServiceDatePage}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global


class RegistrationServiceSpec extends SpecBase with BeforeAndAfterEach {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockAuditService: AuditService = mock[AuditService]
  private val registrationService = new RegistrationService(stubClockAtArbitraryDate, mockRegistrationConnector, mockAuditService)

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
    reset(mockAuditService)
  }

  private def getExclusionDetails(
                                   exclusionReason: ExclusionReason,
                                   exclusionRequestDate: LocalDate,
                                   movePOBDate: Option[LocalDate],
                                   issuedBy: Option[String],
                                   vatNumber: Option[String]
                                 ): EtmpExclusionDetails = {
    EtmpExclusionDetails(
      exclusionRequestDate = exclusionRequestDate,
      exclusionReason = exclusionReason,
      movePOBDate = movePOBDate,
      issuedBy = issuedBy,
      vatNumber = vatNumber
    )
  }

  private def buildRegistration(
                                 exclusionReason: Option[ExclusionReason],
                                 exclusionRequestDate: LocalDate,
                                 movePOBDate: Option[LocalDate],
                                 issuedBy: Option[String],
                                 vatNumber: Option[String]): AmendRegistrationRequest = {
    val exclusionDetails = exclusionReason.map(reason => getExclusionDetails(reason, exclusionRequestDate, movePOBDate, issuedBy, vatNumber))

    amendRegistrationRequest.copy(
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
      submissionReceived = registration.submissionReceived,
      exclusionDetails = exclusionDetails
    )
  }

  ".amendRegistration" - {

    "when transferring of MSID" - {

      "must create registration request and return a successful ETMP enrolment response" in {

        val expectedAmendRegistrationRequest = buildRegistration(
          Some(ExclusionReason.TransferringMSID),
          LocalDate.now(),
          Some(moveDate),
          Some(country.code),
          Some(taxNumber)
        )

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            completeUserAnswers,
            registration,
            Some(ExclusionReason.TransferringMSID),
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
        val expectedAmendRegistrationRequest = buildRegistration(
          Some(ExclusionReason.NoLongerSupplies),
          stoppedSellingGoodsDate,
          None,
          None,
          None
        )

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            userAnswers,
            registration,
            Some(ExclusionReason.NoLongerSupplies),
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

        val exceptedAmendRegistrationRequest = buildRegistration(
          Some(ExclusionReason.VoluntarilyLeaves),
          stoppedUsingServiceDate,
          None,
          None,
          None
        )

        when(mockRegistrationConnector.amend(any())(any())) thenReturn Right(()).toFuture

        val app = applicationBuilder()
          .build()

        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

        running(app) {

          registrationService.amendRegistrationAndAudit(
            userAnswersId,
            vrn,
            userAnswers,
            registration,
            Some(ExclusionReason.VoluntarilyLeaves),
            ExclusionAuditType.ExclusionRequestSubmitted
          ).futureValue mustBe Right(())
          verify(mockRegistrationConnector, times(1)).amend(eqTo(exceptedAmendRegistrationRequest))(any())
        }

      }

    }
  }

}
