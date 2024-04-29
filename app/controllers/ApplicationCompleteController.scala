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

import config.FrontendAppConfig
import controllers.actions._

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView

import java.time.{Clock, LocalDate}
import java.time.format.DateTimeFormatter

class ApplicationCompleteController @Inject()(
                                               cc: AuthenticatedControllerComponents,
                                               config: FrontendAppConfig,
                                               clock: Clock,
                                               view: ApplicationCompleteView
                                             ) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetData {
    implicit request =>

      val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      val leaveDate = dateTimeFormatter.format(LocalDate.now(clock).plusDays(1))
      val cancelDate = dateTimeFormatter.format(LocalDate.now(clock))

      Ok(view(config.ossYourAccountUrl, leaveDate, cancelDate))
  }
}
