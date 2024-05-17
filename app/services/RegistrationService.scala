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

import connectors.RegistrationConnector
import connectors.RegistrationHttpParser.AmendRegistrationResultResponse
import models.audit.{ExclusionAuditModel, ExclusionAuditType, SubmissionResult}
import models.exclusions.ExclusionReason.{NoLongerSupplies, TransferringMSID, VoluntarilyLeaves}
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.registration.Registration
import models.requests.RegistrationRequest
import models.{Period, Quarter, StandardPeriod, UserAnswers}
import pages._
import play.api.mvc.Request
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class RegistrationService @Inject()(
                                     clock: Clock,
                                     registrationConnector: RegistrationConnector,
                                     auditService: AuditService
                                   )(implicit ec: ExecutionContext) {

  def amendRegistrationAndAudit(
                                 userId: String,
                                 vrn: Vrn,
                                 answers: UserAnswers,
                                 registration: Registration,
                                 exclusionReason: Option[ExclusionReason],
                                 exclusionAuditType: ExclusionAuditType
                               )(implicit hc: HeaderCarrier, request: Request[_]): Future[AmendRegistrationResultResponse] = {

    val success: ExclusionAuditModel = ExclusionAuditModel(
      exclusionAuditType = exclusionAuditType,
      userId = userId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      vrn = vrn.vrn,
      userAnswers = answers.toUserAnswersForAudit,
      registration = registration,
      exclusionReason = exclusionReason,
      submissionResult = SubmissionResult.Success
    )
    val failure: ExclusionAuditModel = success.copy(submissionResult = SubmissionResult.Failure)

    amendRegistration(answers, exclusionReason, vrn, registration).andThen {
      case Success(Right(_)) => auditService.audit(success)(hc, request)
      case _ => auditService.audit(failure)(hc, request)
    }
  }

  private def amendRegistration(
                                 answers: UserAnswers,
                                 exclusionReason: Option[ExclusionReason],
                                 vrn: Vrn,
                                 registration: Registration,
                               )(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] = {

    registrationConnector.amend(buildRegistration(answers, exclusionReason, vrn, registration))
  }

  private def buildRegistration(
                                 answers: UserAnswers,
                                 exclusionReason: Option[ExclusionReason],
                                 vrn: Vrn,
                                 registration: Registration,
                               ): RegistrationRequest = {

    RegistrationRequest(
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

  private def getExcludedTrader(exclusionReason: ExclusionReason, answers: UserAnswers, vrn: Vrn): ExcludedTrader = {

    val effectiveDate = exclusionReason match {
      case TransferringMSID =>
        answers.get(MoveDatePage).getOrElse(throw new Exception("No move date provided"))
      case NoLongerSupplies =>
        answers.get(StoppedSellingGoodsDatePage).getOrElse(throw new Exception("No stopped selling goods date provided"))
      case VoluntarilyLeaves =>
        answers.get(StoppedUsingServiceDatePage).getOrElse(throw new Exception("No stopped using service date provided"))
      //todo case EtmpExclusionReason.Reversal =>
      case _ => throw new Exception("Exclusion reason not valid")
    }

    ExcludedTrader(
      vrn = vrn,
      exclusionReason = exclusionReason,
      effectivePeriod = determinePeriod(effectiveDate),
      effectiveDate = effectiveDate
    )
  }

  private def determinePeriod(effectiveDate: LocalDate): Period = {

    val quarter: Try[Quarter] = Quarter.fromString(effectiveDate.format(DateTimeFormatter.ofPattern("QQQ")))

    quarter match {
      case Success(value) =>
        StandardPeriod(effectiveDate.getYear, value)
      case Failure(exception) =>
        throw exception
    }
  }
}
