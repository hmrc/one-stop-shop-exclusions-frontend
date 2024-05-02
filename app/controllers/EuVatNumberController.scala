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

import controllers.actions._
import forms.EuVatNumberFormProvider
import models.{CountryWithValidationDetails, UserAnswers}

import javax.inject.Inject
import pages.{EuCountryPage, EuVatNumberPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.EuVatNumberView

import scala.concurrent.{ExecutionContext, Future}

class EuVatNumberController @Inject()(
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: EuVatNumberFormProvider,
                                       view: EuVatNumberView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData {
    implicit request =>

      getCountryWithValidationDetails(request.userAnswers).map { countryWithValidationDetails =>
        val form = formProvider(countryWithValidationDetails.country)
        val preparedForm = request.userAnswers.get(EuVatNumberPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }


        Ok(view(preparedForm, waypoints, countryWithValidationDetails))
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData.async {
    implicit request =>

      getCountryWithValidationDetails(request.userAnswers).map { countryWithValidationDetails =>
        formProvider(countryWithValidationDetails.country).bindFromRequest().fold(
          formWithErrors => BadRequest(view(formWithErrors, waypoints, countryWithValidationDetails)).toFuture,

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(EuVatNumberPage, value))
              _              <- cc.sessionRepository.set(updatedAnswers)
            } yield Redirect(EuVatNumberPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
        )
      }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
  }

  private def getCountryWithValidationDetails(userAnswers: UserAnswers): Option[CountryWithValidationDetails] = {
    userAnswers.get(EuCountryPage).flatMap(country =>
      CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == country.code)
    )
  }
}
