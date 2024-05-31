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
import date.Dates
import forms.MoveDateFormProvider

import javax.inject.Inject
import pages.{EuCountryPage, MoveDatePage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.MoveDateView

import scala.concurrent.{ExecutionContext, Future}

class MoveDateController @Inject()(
                                    cc: AuthenticatedControllerComponents,
                                    formProvider: MoveDateFormProvider,
                                    dates: Dates,
                                    view: MoveDateView
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData {
    implicit request =>

      val form = formProvider()

      val preparedForm = request.userAnswers.get(MoveDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(EuCountryPage).map { country =>
        Ok(view(
          preparedForm,
          waypoints,
          country,
          dates.formatter.format(dates.maxMoveDate),
          dates.dateHint
        ))
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData.async {
    implicit request =>

      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          request.userAnswers.get(EuCountryPage).map { country =>
            BadRequest(view(
              formWithErrors,
              waypoints,
              country,
              dates.formatter.format(dates.maxMoveDate),
              dates.dateHint
            )).toFuture
          }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()).toFuture),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MoveDatePage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(MoveDatePage.navigate(waypoints, updatedAnswers, updatedAnswers).url)
      )
  }
}
