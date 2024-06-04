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

package base

import controllers.actions._
import date.Dates
import generators.Generators
import models.CountryWithValidationDetails.euCountriesWithVRNValidationRules
import models.registration.Registration
import models.requests.RegistrationRequest
import models.{CheckMode, Country, CountryWithValidationDetails, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.BodyParsers
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, LocalDate, ZoneId}

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience
    with Generators {

  val userAnswersId: String = "id"
  val emptyWaypoints: Waypoints = EmptyWaypoints
  val checkModeWaypoints: Waypoints = emptyWaypoints.setNextWaypoint(Waypoint(CheckYourAnswersPage, CheckMode, CheckYourAnswersPage.urlFragment))
  val country: Country = arbitraryCountry.arbitrary.sample.value
  val anotherCountry: Country = Gen.oneOf(Country.euCountries.filterNot(_ == country)).sample.value
  val moveDate: LocalDate = LocalDate.now(Dates.clock)
  val euVatNumber: String = getEuVatNumber(country.code)
  val taxNumber: String = "213456789"
  val countryWithValidationDetails: CountryWithValidationDetails =
    euCountriesWithVRNValidationRules.find(_.country == country).value
  val vrn: Vrn = Vrn(countryWithValidationDetails.exampleVrn)
  val registration: Registration = Arbitrary.arbitrary[Registration].sample.value
  val registrationRequest: RegistrationRequest = arbitrary[RegistrationRequest].sample.value
  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)

  def completeUserAnswers: UserAnswers =
    emptyUserAnswers
      .set(MoveCountryPage, true).success.value
      .set(EuCountryPage, country).success.value
      .set(MoveDatePage, moveDate).success.value
      .set(EuVatNumberPage, taxNumber).success.value


  def emptyUserAnswers : UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None,
                                    maybeRegistration: Option[Registration] = None
                                  ): GuiceApplicationBuilder = {
    val application = new GuiceApplicationBuilder()
    val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
      application
        .overrides(
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers, vrn, maybeRegistration.getOrElse(registration))),
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, vrn, maybeRegistration.getOrElse(registration)))
        )
  }

  def getEuVatNumber(countryCode: String): String =
    CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == countryCode).map { matchedCountryRule =>
      s"$countryCode${matchedCountryRule.exampleVrn}"
    }.value
}
