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

package controllers.actions

import controllers.routes
import logging.Logging
import models.UserAnswers
import models.requests.OptionalDataRequest
import pages.MoveCountryPage
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersFilterImpl @Inject()(implicit val executionContext: ExecutionContext)
  extends CheckYourAnswersFilter with Logging {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(MoveCountryPage) match {
      case None => Future.successful(None)
      case Some(value) if !value && request.uri.contains(routes.CheckYourAnswersController.onPageLoad().url) =>
        Future.successful(Some(Redirect(routes.KickOutController.onPageLoad())))
      case Some(_) => Future.successful(None)
    }
    preparedForm
  }
}

trait CheckYourAnswersFilter extends ActionFilter[OptionalDataRequest]