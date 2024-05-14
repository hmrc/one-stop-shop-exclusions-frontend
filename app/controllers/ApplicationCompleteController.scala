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
import date.{Dates, LocalDateOps}
import models.requests.DataRequest
import pages._

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationCompleteView

import java.time.LocalDate

class ApplicationCompleteController @Inject()(
                                               cc: AuthenticatedControllerComponents,
                                               config: FrontendAppConfig,
                                               dates: Dates,
                                               view: ApplicationCompleteView
                                             ) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad: Action[AnyContent] = cc.authAndGetData {
    implicit request =>

      request.userAnswers.get(MoveCountryPage).flatMap { isMovingCountry =>
        if (isMovingCountry) {
          onMovingBusiness()
        } else {
          request.userAnswers.get(StopSellingGoodsPage).flatMap { stopSellingGoods =>
            if (stopSellingGoods) {
              onStopSellingGoods()
            } else {
              onStopUsingService()
            }
          }
        }
      }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  private def onMovingBusiness()(implicit request: DataRequest[AnyContent]): Option[Result] = {
    val messages: Messages = implicitly[Messages]

    for {
      country <- request.userAnswers.get(EuCountryPage)
      leaveDate <- request.userAnswers.get(MoveDatePage)
    } yield {
      val maxChangeDate = leaveDate.plusMonths(1).withDayOfMonth(dates.MoveDayOfMonthSplit)
      val isDateBeforeToday = leaveDate <= LocalDate.now()
      val isDateBeforeCurrentPeriod = leaveDate <= dates.firstDayOfQuarter

      val nextInfoBullet = if (!isDateBeforeCurrentPeriod) {
        Some(messages("applicationComplete.next.info.bullet0", country.name, dates.formatter.format(maxChangeDate)))
      } else {
        None
      }

      val leaveMessage = (isDateBeforeToday, isDateBeforeCurrentPeriod) match {
        case (true, true) => Some(messages("applicationComplete.leave.text", dates.formatter.format(leaveDate)))
        case (true, false) => Some(messages("applicationComplete.left.text"))
        case _ => Some(messages("applicationComplete.leave.text", dates.formatter.format(leaveDate)))
      }

      val nextInfoBottom = if (!isDateBeforeCurrentPeriod) {
        Some(messages("applicationComplete.next.info.bottom", dates.formatter.format(maxChangeDate)))
      } else {
        Some(messages("applicationComplete.next.info.bottom.continue", country.name, dates.formatter.format(maxChangeDate)))
      }

      Ok(view(
        config.ossYourAccountUrl,
        dates.formatter.format(leaveDate),
        dates.formatter.format(maxChangeDate),
        Some(messages("applicationComplete.moving.text", country.name)),
        nextInfoBullet,
        leaveMessage,
        nextInfoBottom
      ))
    }
  }


  private def onStopSellingGoods()(implicit request: DataRequest[_]): Option[Result] = {
    val messages: Messages = implicitly[Messages]

    request.userAnswers.get(StoppedSellingGoodsDatePage).map { _ =>
      val leaveDate = dates.getLeaveDateWhenStoppedSellingGoods
      Ok(view(
        config.ossYourAccountUrl,
        dates.formatter.format(leaveDate),
        dates.formatter.format(leaveDate.minusDays(1)),
        Some(messages("applicationComplete.stopSellingGoods.text")),
        None,
        Some(messages("applicationComplete.leave.text", dates.formatter.format(leaveDate))),
        Some(messages("applicationComplete.next.info.bottom", dates.formatter.format(leaveDate.minusDays(1))))

      ))
    }
  }

  private def onStopUsingService()(implicit request: DataRequest[_]): Option[Result] = {
    val messages: Messages = implicitly[Messages]

    request.userAnswers.get(StoppedUsingServiceDatePage).map { stoppedUsingServiceDate =>
      val leaveDate = dates.getLeaveDateWhenStoppedUsingService(stoppedUsingServiceDate)
      Ok(view(
        config.ossYourAccountUrl,
        dates.formatter.format(leaveDate),
        dates.formatter.format(leaveDate.minusDays(1)),
        None,
        None,
        Some(messages("applicationComplete.leave.text", dates.formatter.format(leaveDate))),
        Some(messages("applicationComplete.next.info.bottom", dates.formatter.format(leaveDate.minusDays(1))))
      ))
    }
  }
}
