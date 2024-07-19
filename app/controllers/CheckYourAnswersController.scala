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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.AuthenticatedControllerComponents
import date.Dates
import logging.Logging
import models.{CheckMode, UserAnswers}
import models.audit.ExclusionAuditType
import models.exclusions.ExclusionReason
import pages.{CheckYourAnswersPage, EmptyWaypoints, MoveCountryPage, StopSellingGoodsPage, Waypoint, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.{EuCountrySummary, EuVatNumberSummary, MoveCountrySummary, MoveDateSummary, StopSellingGoodsSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext


class CheckYourAnswersController @Inject()(
                                            cc: AuthenticatedControllerComponents,
                                            dates: Dates,
                                            view: CheckYourAnswersView,
                                            registrationService: RegistrationService,
                                            config: FrontendAppConfig
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with CompletionChecks with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = cc.authAndGetDataWithCYA {
    implicit request =>

      val thisPage = CheckYourAnswersPage

      val waypoints = EmptyWaypoints.setNextWaypoint(Waypoint(thisPage, CheckMode, CheckYourAnswersPage.urlFragment))

      val euCountrySummaryRow = EuCountrySummary.countryRow(request.userAnswers, waypoints, thisPage)
      val moveDateSummaryRow = MoveDateSummary.rowMoveDate(request.userAnswers, waypoints, thisPage, dates)
      val euVatNumberSummaryRow = EuVatNumberSummary.rowEuVatNumber(request.userAnswers, waypoints, thisPage)
      val moveCountrySummaryRow = MoveCountrySummary.row(request.userAnswers, waypoints, thisPage)
      val stopSellingGoodsSummaryRow = StopSellingGoodsSummary.row(request.userAnswers, waypoints, thisPage)

      val list = SummaryListViewModel(
        rows = Seq(
          moveCountrySummaryRow,
          stopSellingGoodsSummaryRow,
          euCountrySummaryRow,
          moveDateSummaryRow,
          euVatNumberSummaryRow
        ).flatten
      )

      val isValid = validate()

      Ok(view(waypoints, list, isValid, config.ossYourAccountUrl))
  }

  def onSubmit(waypoints: Waypoints, incompletePrompt: Boolean): Action[AnyContent] = cc.authAndGetData.async {
    implicit request =>
      getFirstValidationErrorRedirect(waypoints) match {
        case Some(errorRedirect) => if (incompletePrompt) {
          errorRedirect.toFuture
        } else {
          Redirect(routes.CheckYourAnswersController.onPageLoad()).toFuture
        }

        case None =>
          val exclusionReason = determineExclusionReason(request.userAnswers)

          registrationService.amendRegistrationAndAudit(
            request.userId,
            request.vrn,
            request.userAnswers,
            request.registration,
            Some(exclusionReason),
            ExclusionAuditType.ExclusionRequestSubmitted
          ).map {
            case Right(_) =>
              Redirect(CheckYourAnswersPage.navigate(waypoints, request.userAnswers, request.userAnswers).route)
            case Left(e) =>
              logger.error(s"Failure to submit self exclusion ${e.body}")
              Redirect(routes.SubmissionFailureController.onPageLoad())
          }
      }
  }

  private def determineExclusionReason(userAnswers: UserAnswers): ExclusionReason = {
    userAnswers.get(MoveCountryPage) match {
      case Some(true) =>
        ExclusionReason.TransferringMSID
      case Some(false) =>
        userAnswers.get(StopSellingGoodsPage) match {
          case Some(true) =>
            ExclusionReason.NoLongerMeetsConditions
          case Some(false) =>
            ExclusionReason.VoluntarilyLeaves
          case _ =>
            throw new Exception("Expected stop selling goods page")
        }
      case _ =>
        throw new Exception("Expected move country page")
    }
  }
}
