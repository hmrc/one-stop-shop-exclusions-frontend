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
import forms.StoppedUsingServiceDateFormProvider
import logging.Logging
import pages.{StoppedUsingServiceDatePage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.StoppedUsingServiceDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StoppedUsingServiceDateController @Inject()(
                                                   cc: AuthenticatedControllerComponents,
                                                   formProvider: StoppedUsingServiceDateFormProvider,
                                                   dates: Dates,
                                                   view: StoppedUsingServiceDateView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData {
    implicit request =>
      val form = formProvider(dates.today.date, dates.getMinimumDateBasedOnCommencementDate())

      val preparedForm = request.userAnswers.get(StoppedUsingServiceDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints, dates.lastDayOfQuarterFormatted, dates.dateHint))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData.async {
    implicit request =>

      val form = formProvider(dates.today.date, dates.getMinimumDateBasedOnCommencementDate())

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints, dates.lastDayOfQuarterFormatted, dates.dateHint)).toFuture,

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StoppedUsingServiceDatePage, value))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(StoppedUsingServiceDatePage.navigate(waypoints, request.userAnswers, updatedAnswers).url)
      )
  }

}
