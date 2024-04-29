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

package viewmodels.checkAnswers

import date.Dates
import models.UserAnswers
import pages.{CheckAnswersPage, MoveDatePage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MoveDateSummary {

  def rowMoveDate(answers: UserAnswers,
                  waypoints: Waypoints,
                  sourcePage: CheckAnswersPage,
                  dates: Dates)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MoveDatePage).map {
      moveDate =>
        val value = dates.formatter.format(moveDate)

        val moveDatePageChangeUrl = MoveDatePage.changeLink(waypoints, sourcePage).url
        SummaryListRowViewModel(
          key = "moveDate.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", moveDatePageChangeUrl)
              .withVisuallyHiddenText(messages("moveDate.change.hidden"))
          )
        )
    }
}
