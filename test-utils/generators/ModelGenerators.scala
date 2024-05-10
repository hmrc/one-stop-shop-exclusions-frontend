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

package generators

import models.etmp.{NonCompliantDetails, _}
import models.registration._
import models.{etmp, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.option
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

trait ModelGenerators {

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      Gen.oneOf(Country.euCountries)
    }

  implicit lazy val arbitrarySalesChannels: Arbitrary[SalesChannels] =
    Arbitrary {
      Gen.oneOf(SalesChannels.values)
    }

  implicit lazy val arbitraryNiPresence: Arbitrary[NiPresence] =
    Arbitrary {
      Gen.oneOf(
        Gen.const(PrincipalPlaceOfBusinessInNi),
        Gen.const(FixedEstablishmentInNi),
        arbitrary[SalesChannels].map(NoPresence(_))
      )
    }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }

  implicit lazy val arbitraryIban: Arbitrary[Iban] =
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).toOption.get)
    }

  implicit lazy val arbitraryVatDetails: Arbitrary[VatDetails] =
    Arbitrary {
      for {
        registrationDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        address <- arbitrary[Address]
        partOfVatGroup <- arbitrary[Boolean]
        source <- arbitrary[VatDetailSource]
      } yield VatDetails(registrationDate, address, partOfVatGroup, source)
    }

  implicit val arbitraryVatDetailSource: Arbitrary[VatDetailSource] =
    Arbitrary(
      Gen.oneOf(VatDetailSource.values)
    )

  implicit lazy val arbitraryPreviousSchemeDetails: Arbitrary[PreviousSchemeDetails] =
    Arbitrary {
      for {
        previousScheme <- Gen.oneOf(PreviousScheme.values)
        previousSchemeNumber <- Gen.listOfN(11, Gen.alphaChar).map(_.mkString)
      } yield PreviousSchemeDetails(previousScheme, PreviousSchemeNumbers(previousSchemeNumber, None))
    }

  implicit lazy val arbitraryPreviousRegistration: Arbitrary[PreviousRegistration] =
    Arbitrary {
      Gen.oneOf(arbitrary[PreviousRegistrationNew], arbitrary[PreviousRegistrationLegacy])
    }

  implicit lazy val arbitraryPreviousRegistrationNew: Arbitrary[PreviousRegistrationNew] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        previousSchemeDetails <- Gen.listOfN(2, arbitrary[PreviousSchemeDetails])
      } yield PreviousRegistrationNew(country, previousSchemeDetails)
    }

  implicit lazy val arbitraryPreviousRegistrationLegacy: Arbitrary[PreviousRegistrationLegacy] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        vatNumber <- arbitrary[String]
      } yield PreviousRegistrationLegacy(country, vatNumber)
    }

  implicit lazy val arbitraryEuTaxRegistration: Arbitrary[EuTaxRegistration] =
    Arbitrary {
      Gen.oneOf(arbitrary[RegistrationWithFixedEstablishment].sample)
    }

  implicit lazy val arbitraryEuVatRegistration: Arbitrary[EuVatRegistration] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        vatNumber <- arbitrary[String]
      } yield EuVatRegistration(country, vatNumber)
    }

  implicit lazy val arbitraryRegistrationWithoutFixedEstablishmentWithTradeDetails: Arbitrary[RegistrationWithoutFixedEstablishmentWithTradeDetails] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        taxIdentifier <- arbitrary[EuTaxIdentifier]
        tradingName <- arbitrary[String]
        address <- arbitraryInternationalAddress.arbitrary
      } yield RegistrationWithoutFixedEstablishmentWithTradeDetails(country,
        taxIdentifier,
        TradeDetails(
          tradingName,
          address)
      )
    }

  implicit lazy val arbitraryRegistrationWithFixedEstablishment: Arbitrary[RegistrationWithFixedEstablishment] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        taxIdentifier <- arbitrary[EuTaxIdentifier]
        fixedEstablishment <- arbitrary[TradeDetails]
      } yield RegistrationWithFixedEstablishment(country, taxIdentifier, fixedEstablishment)
    }

  implicit lazy val arbitraryRegistrationWithoutTaxId: Arbitrary[RegistrationWithoutTaxId] =
    Arbitrary {
      arbitrary[Country].map(c => RegistrationWithoutTaxId(c))
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county <- Gen.option(arbitrary[String])
        postCode <- arbitrary[String]
      } yield UkAddress(line1, line2, townOrCity, county, postCode)
    }

  implicit val arbitraryAddress: Arbitrary[Address] =
    Arbitrary {
      Gen.oneOf(
        arbitrary[UkAddress],
        arbitrary[InternationalAddress],
        arbitrary[DesAddress]
      )
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        country <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        line3 <- Gen.option(arbitrary[String])
        line4 <- Gen.option(arbitrary[String])
        line5 <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
      } yield DesAddress(line1, line2, line3, line4, line5, postCode, countryCode)
    }

  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- Gen.option(arbitrary[Bic])
        iban <- arbitrary[Iban]
      } yield etmp.BankDetails(accountName, bic, iban)
    }

  implicit lazy val arbitraryAdminUse: Arbitrary[AdminUse] =
    Arbitrary {
      for {
        changeDate <- arbitrary[LocalDateTime]
      } yield AdminUse(Some(changeDate))
    }

  implicit val arbitraryEuTaxIdentifierType: Arbitrary[EuTaxIdentifierType] =
    Arbitrary {
      Gen.oneOf(EuTaxIdentifierType.values)
    }

  implicit val arbitraryEuTaxIdentifier: Arbitrary[EuTaxIdentifier] =
    Arbitrary {
      for {
        identifierType <- arbitrary[EuTaxIdentifierType]
        value <- arbitrary[Int].map(_.toString)
      } yield EuTaxIdentifier(identifierType, value)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[TradeDetails] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address <- arbitrary[InternationalAddress]
      } yield TradeDetails(tradingName, address)
    }

  implicit lazy val arbitraryBusinessContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        fullName <- arbitrary[String]
        telephoneNumber <- arbitrary[String]
        emailAddress <- arbitrary[String]
      } yield ContactDetails(fullName, telephoneNumber, emailAddress)
    }

  implicit val arbitraryVrn: Arbitrary[Vrn] =
    Arbitrary {
      Gen.listOfN(9, Gen.numChar).map(_.mkString).map(Vrn)
    }

  implicit val arbitraryEtmpTradingNames: Arbitrary[EtmpTradingNames] = {
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
      } yield EtmpTradingNames(tradingName)
    }
  }

  implicit lazy val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf[EtmpExclusionReason](EtmpExclusionReason.values)
        effectiveDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        decisionDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        quarantine <- arbitrary[Boolean]
      } yield EtmpExclusion(
        exclusionReason,
        effectiveDate,
        decisionDate,
        quarantine
      )
    }
  }

  implicit val arbitraryDisplayEtmpEuRegistrationDetails: Arbitrary[EtmpDisplayEuRegistrationDetails] = {
    Arbitrary {
      for {
        countryOfRegistration <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString.toUpperCase)
        traderId <- arbitrary[String]
        tradingName <- arbitrary[String]
        fixedEstablishmentAddressLine1 <- arbitrary[String]
        fixedEstablishmentAddressLine2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        regionOrState <- Gen.option(arbitrary[String])
        postcode <- Gen.option(arbitrary[String])
      } yield {
        EtmpDisplayEuRegistrationDetails(
          countryOfRegistration,
          Some(traderId),
          None,
          tradingName,
          fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2,
          townOrCity,
          regionOrState,
          postcode
        )
      }
    }
  }

  implicit val arbitraryEtmpEuRegistrationDetails: Arbitrary[EtmpEuRegistrationDetails] = {
    Arbitrary {
      for {
        countryOfRegistration <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString.toUpperCase)
        vatNumber <- Gen.option(arbitraryVrn.arbitrary.toString)
        taxIdentificationNumber <- Gen.option(arbitrary[String])
        fixedEstablishment <- Gen.option(arbitrary[Boolean])
        tradingName <- Gen.option(arbitrary[String])
        fixedEstablishmentAddressLine1 <- Gen.option(arbitrary[String])
        fixedEstablishmentAddressLine2 <- Gen.option(arbitrary[String])
        townOrCity <- Gen.option(arbitrary[String])
        regionOrState <- Gen.option(arbitrary[String])
        postcode <- Gen.option(arbitrary[String])
      } yield {
        EtmpEuRegistrationDetails(
          countryOfRegistration = countryOfRegistration,
          vatNumber = vatNumber,
          taxIdentificationNumber = taxIdentificationNumber,
          fixedEstablishment = fixedEstablishment,
          tradingName = tradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2 = fixedEstablishmentAddressLine2,
          townOrCity = townOrCity,
          regionOrState = regionOrState,
          postcode = postcode
        )
      }
    }
  }

  implicit val arbitraryEtmpPreviousEURegistrationDetails: Arbitrary[EtmpPreviousEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitrary[String]
        registrationNumber <- arbitrary[String]
        schemeType <- Gen.oneOf(SchemeType.values)
        intermediaryNumber <- Gen.option(arbitrary[String])
      } yield EtmpPreviousEuRegistrationDetails(issuedBy, registrationNumber, schemeType, intermediaryNumber)
    }
  }

  implicit val arbitraryWebsite: Arbitrary[Website] = {
    Arbitrary {
      for {
        websiteAddress <- arbitrary[String]
      } yield Website(websiteAddress)
    }
  }

  implicit val arbitraryEtmpSchemeDetails: Arbitrary[EtmpSchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate]
        euRegistrationDetails <- Gen.listOfN(5, arbitraryEtmpEuRegistrationDetails.arbitrary)
        previousEURegistrationDetails <- Gen.listOfN(5, arbitraryEtmpPreviousEURegistrationDetails.arbitrary)
        websites <- Gen.listOfN(10, arbitraryWebsite.arbitrary)
        contactName <- arbitrary[String]
        businessTelephoneNumber <- arbitrary[String]
        businessEmailId <- arbitrary[String]
        nonCompliantReturns <- Gen.option(arbitrary[Int].toString)
        nonCompliantPayments <- Gen.option(arbitrary[Int].toString)
      } yield
        EtmpSchemeDetails(
          commencementDate,
          euRegistrationDetails,
          previousEURegistrationDetails,
          websites,
          contactName,
          businessTelephoneNumber,
          businessEmailId,
          nonCompliantReturns,
          nonCompliantPayments
        )
    }
  }

   def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  implicit val arbitraryRegistration: Arbitrary[Registration] =
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        name <- arbitrary[String]
        vatDetails <- arbitrary[VatDetails]

        contactDetails <- arbitrary[ContactDetails]
        bankDetails <- arbitrary[BankDetails]
        commencementDate <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.now)
        isOnlineMarketplace <- arbitrary[Boolean]
        adminUse <- arbitrary[AdminUse]
      } yield Registration(vrn, name, Nil, vatDetails, Nil, contactDetails, Nil, commencementDate, Nil, bankDetails, isOnlineMarketplace, None, None, None, None, None, None, None, None, adminUse)
    }

  implicit val arbitraryStandardPeriod: Arbitrary[StandardPeriod] =
    Arbitrary {
      for {
        year <- Gen.choose(2022, 2099)
        quarter <- Gen.oneOf(Quarter.values)
      } yield StandardPeriod(year, quarter)
    }

  implicit lazy val arbitraryEtmpCustomerIdentification: Arbitrary[EtmpAmendCustomerIdentification] =
    Arbitrary {
      for {
        vrn <- arbitraryVrn.arbitrary
      } yield EtmpAmendCustomerIdentification(vrn)
    }

  implicit lazy val arbitrarySchemeType: Arbitrary[SchemeType] =
    Arbitrary {
      Gen.oneOf(SchemeType.values)
    }

  implicit lazy val arbitraryNonCompliantDetails: Arbitrary[NonCompliantDetails] =
    Arbitrary {
      for {
        nonCompliantReturns <- option(Gen.chooseNum(1, 2))
        nonCompliantPayments <- option(Gen.chooseNum(1, 2))
      } yield {
        NonCompliantDetails(
          nonCompliantReturns = nonCompliantReturns,
          nonCompliantPayments = nonCompliantPayments
        )
      }
    }
}
