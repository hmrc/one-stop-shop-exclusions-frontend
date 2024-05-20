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
import forms.CancelLeaveSchemeRequestFormProvider
import logging.Logging
import models.UserAnswers
import models.audit.ExclusionAuditType
import models.exclusions.ExclusionReason
import models.requests.OptionalDataRequest
import pages.{CancelLeaveSchemeRequestPage, CancelLeaveSchemeSubmissionFailurePage, Waypoints}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.CancelLeaveSchemeRequestView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancelLeaveSchemeRequestController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    cc: AuthenticatedControllerComponents,
                                                    frontendAppConfig: FrontendAppConfig,
                                                    formProvider: CancelLeaveSchemeRequestFormProvider,
                                                    registrationService: RegistrationService,
                                                    view: CancelLeaveSchemeRequestView
                                                  )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc
  val form: Form[Boolean] = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetOptionalDataAndEvaluateExcludedTrader {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(CancelLeaveSchemeRequestPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.authAndGetOptionalDataAndEvaluateExcludedTrader.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints)).toFuture,

        value => {
          val originalAnswers: UserAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
          for {
            updatedAnswers <- Future.fromTry(originalAnswers.set(CancelLeaveSchemeRequestPage, value))
            _ <- cc.sessionRepository.set(updatedAnswers)
            amendRegistrationAndRedirect <- determineRedirect(waypoints, value, originalAnswers, updatedAnswers)
          } yield amendRegistrationAndRedirect
        }
      )
  }

  private def determineRedirect(
                                 waypoints: Waypoints,
                                 cancelLeaveRequest: Boolean,
                                 originalAnswers: UserAnswers,
                                 updatedAnswers: UserAnswers
                               )(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    if (cancelLeaveRequest) {
      registrationService.amendRegistrationAndAudit(
        userId = request.userId,
        vrn = request.vrn,
        answers = updatedAnswers,
        registration = request.registration,
        exclusionReason = Some(ExclusionReason.Reversal),
        exclusionAuditType = ExclusionAuditType.ReversalRequestSubmitted
      ).map {
        case Right(_) =>
          Redirect(CancelLeaveSchemeRequestPage.navigate(waypoints, originalAnswers, updatedAnswers).route)
        case Left(error) =>
          logger.error(s"Failed to update self exclusion status with error: ${error.body}")
          Redirect(CancelLeaveSchemeSubmissionFailurePage.route(waypoints).url)
      }
    } else {
      Redirect(frontendAppConfig.ossYourAccountUrl).toFuture
    }
  }
}
