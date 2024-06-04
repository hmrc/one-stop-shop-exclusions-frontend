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

import logging.Logging
import models.exclusions.EtmpExclusionReason.Reversal
import models.exclusions.ExcludedTrader
import models.requests.OptionalDataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

class CheckAlreadyLeftSchemeFilterImpl @Inject()(clock: Clock)(implicit val executionContext: ExecutionContext)
  extends CheckAlreadyLeftSchemeFilter with Logging {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {

    val maybeExclusion: Option[ExcludedTrader] = request.registration.excludedTrader

    if (maybeExclusion.isEmpty || maybeExclusion.exists(_.exclusionReason == Reversal)) {
      None.toFuture
    } else {
      Some(Redirect(controllers.routes.AlreadyLeftSchemeErrorController.onPageLoad().url)).toFuture
    }
  }
}

trait CheckAlreadyLeftSchemeFilter extends ActionFilter[OptionalDataRequest]

