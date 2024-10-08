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

package controllers.reversals

import config.FrontendAppConfig
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.reversals.CancelLeaveSchemeAcknowledgementView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CancelLeaveSchemeAcknowledgementController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            cc: AuthenticatedControllerComponents,
                                                            frontendAppConfig: FrontendAppConfig,
                                                            view: CancelLeaveSchemeAcknowledgementView,
                                                            sessionRepository: SessionRepository
                                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetOptionalDataAndEvaluateExcludedTrader.async {
    implicit request =>
      request.userAnswers match {
        case Some(userAnswers) =>
          sessionRepository.clear(userAnswers.id).map { _ =>
            Ok(view(frontendAppConfig.ossYourAccountUrl))
          }
        case None =>
          Ok(view(frontendAppConfig.ossYourAccountUrl)).toFuture
      }
  }
}
