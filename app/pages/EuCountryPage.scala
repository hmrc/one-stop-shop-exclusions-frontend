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
import models.{Country, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object EuCountryPage extends QuestionPage[Country] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "euCountry"

  override def route(waypoints: Waypoints): Call =
    routes.EuCountryController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    MoveDatePage

  override def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    if (originalAnswers.get(this).isDefined && (originalAnswers.get(this) != updatedAnswers.get(this))) {
      EuVatNumberPage
    } else {
      super.nextPageCheckMode(waypoints, originalAnswers, updatedAnswers)
    }
  }
}
