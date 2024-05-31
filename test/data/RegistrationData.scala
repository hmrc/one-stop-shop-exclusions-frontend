/*
 * Copyright 2023 HM Revenue & Customs
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

package data

import base.SpecBase
import models.etmp._
import models.{Bic, Country, Iban}
import models.requests.EtmpAmendRegistrationRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import java.time.{LocalDate, LocalDateTime}

trait RegistrationData {
  self: SpecBase =>

  val etmpEuRegistrationDetails: EtmpEuRegistrationDetails = arbitrary[EtmpEuRegistrationDetails].sample.value

  val etmpDisplayEuRegistrationDetails: EtmpDisplayEuRegistrationDetails = arbitrary[EtmpDisplayEuRegistrationDetails].sample.value

  val etmpEuPreviousRegistrationDetails: EtmpPreviousEuRegistrationDetails = EtmpPreviousEuRegistrationDetails(
    issuedBy = arbitrary[Country].sample.value.code,
    registrationNumber = arbitrary[String].sample.value,
    schemeType = arbitrary[SchemeType].sample.value,
    intermediaryNumber = Some(arbitrary[String].sample.value)
  )

  val etmpSchemeDetails: EtmpSchemeDetails = EtmpSchemeDetails(
    commencementDate = LocalDate.now,
    euRegistrationDetails = Seq(etmpEuRegistrationDetails),
    previousEURegistrationDetails = Seq(etmpEuPreviousRegistrationDetails),
    websites = Seq(arbitrary[Website].sample.value),
    contactName = arbitrary[String].sample.value,
    businessTelephoneNumber = arbitrary[String].sample.value,
    businessEmailId = arbitrary[String].sample.value,
    nonCompliantReturns = Some(arbitraryNonCompliantDetails.arbitrary.sample.value.nonCompliantReturns.toString),
    nonCompliantPayments = Some(arbitraryNonCompliantDetails.arbitrary.sample.value.nonCompliantPayments.toString)
  )

  val etmpDisplaySchemeDetails: EtmpDisplaySchemeDetails = EtmpDisplaySchemeDetails(
    commencementDate = LocalDate.now.toString,
    euRegistrationDetails = Seq(etmpDisplayEuRegistrationDetails),
    previousEURegistrationDetails = Seq(etmpEuPreviousRegistrationDetails),
    websites = Seq(arbitrary[Website].sample.value),
    contactName = arbitrary[String].sample.value,
    businessTelephoneNumber = arbitrary[String].sample.value,
    businessEmailId = arbitrary[String].sample.value,
    unusableStatus = false,
    nonCompliantReturns = Some(arbitraryNonCompliantDetails.arbitrary.sample.value.nonCompliantReturns.toString),
    nonCompliantPayments = Some(arbitraryNonCompliantDetails.arbitrary.sample.value.nonCompliantPayments.toString)
  )

  val genBankDetails: BankDetails = BankDetails(
    accountName = arbitrary[String].sample.value,
    bic = Some(arbitrary[Bic].sample.value),
    iban = arbitrary[Iban].sample.value
  )

  val etmpAdminUse: AdminUse = AdminUse(
    changeDate = Some(LocalDateTime.now())
  )

  val etmpDisplayRegistration: EtmpDisplayRegistration = EtmpDisplayRegistration(
    tradingNames = Gen.listOfN(10, arbitraryEtmpTradingNames.arbitrary).sample.value,
    schemeDetails = etmpDisplaySchemeDetails,
    bankDetails = genBankDetails,
    exclusions = Gen.listOfN(3, arbitrary[EtmpExclusion]).sample.value,
    adminUse = etmpAdminUse
  )

  val etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest = EtmpAmendRegistrationRequest(
    administration = EtmpAdministration(EtmpMessageType.OSSSubscriptionAmend),
    changeLog = EtmpAmendRegistrationChangeLog(
      tradingNames = true,
      fixedEstablishments = true,
      contactDetails = true,
      bankDetails = true,
      reRegistration = false
    ),
    customerIdentification = arbitrary[EtmpAmendCustomerIdentification].sample.value,
    tradingNames = Seq(arbitrary[EtmpTradingNames].sample.value),
    schemeDetails = etmpSchemeDetails,
    bankDetails = genBankDetails,
    exclusionDetails = None
  )
}