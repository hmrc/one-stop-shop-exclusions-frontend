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
import forms.StoppedSellingGoodsDateFormProvider

import javax.inject.Inject
import pages.{StoppedSellingGoodsDatePage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.StoppedSellingGoodsDateView

import scala.concurrent.{ExecutionContext, Future}

class StoppedSellingGoodsDateController @Inject()(
                                                   cc: AuthenticatedControllerComponents,
                                                   formProvider: StoppedSellingGoodsDateFormProvider,
                                                   dates: Dates,
                                                   view: StoppedSellingGoodsDateView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData {
    implicit request =>

      val form = formProvider()

      val preparedForm = request.userAnswers.get(StoppedSellingGoodsDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints, dates.lastDayOfQuarterFormatted, dates.dateHint))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetData.async {
    implicit request =>

      val form = formProvider()

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints, dates.lastDayOfQuarterFormatted, dates.dateHint)).toFuture,

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StoppedSellingGoodsDatePage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(StoppedSellingGoodsDatePage.navigate(waypoints, updatedAnswers, updatedAnswers).url)
      )
  }
}
