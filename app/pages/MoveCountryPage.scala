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

package pages

import controllers.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object MoveCountryPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "moveCountry"

  override def route(waypoints: Waypoints): Call =
    routes.MoveCountryController.onPageLoad(waypoints)

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(true) => for {
        removedStoppedSellingGoodsDatePageUA <- userAnswers.remove(StoppedSellingGoodsDatePage)
        updatedUserAnswers <- removedStoppedSellingGoodsDatePageUA.remove(StoppedUsingServiceDatePage)
      } yield updatedUserAnswers
      case Some(false) => for {
        removedEuCountryAnswers <- userAnswers.remove(EuCountryPage)
        updatedUserAnswers <- removedEuCountryAnswers.remove(EuVatNumberPage)
      } yield updatedUserAnswers
      case _ => super.cleanup(value, userAnswers)
    }
  }

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true => EuCountryPage
      case false => StopSellingGoodsPage
    }.orRecover
}
