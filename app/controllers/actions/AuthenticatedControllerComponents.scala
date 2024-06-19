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

import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait AuthenticatedControllerComponents extends MessagesControllerComponents {

  def actionBuilder: DefaultActionBuilder
  def sessionRepository: SessionRepository
  def identify: IdentifierAction
  def getData: DataRetrievalAction
  def requireData: DataRequiredAction
  def checkExcludedTrader: CheckCancelRequestToLeaveFilter
  def checkAlreadyLeft: CheckAlreadyLeftSchemeFilter
  def checkYourAnswers: CheckYourAnswersFilter

  def auth: ActionBuilder[IdentifierRequest, AnyContent] =
    actionBuilder andThen identify

  def authAndGetData: ActionBuilder[DataRequest, AnyContent] =
    authAndGetOptionalData andThen requireData

  def authAndGetDataWithCYA: ActionBuilder[DataRequest, AnyContent] =
    authAndGetOptionalData andThen checkYourAnswers andThen requireData

  def authAndGetOptionalData: ActionBuilder[OptionalDataRequest, AnyContent] =
    auth andThen getData

  def authAndGetOptionalDataAndEvaluateExcludedTrader: ActionBuilder[OptionalDataRequest, AnyContent] =
    authAndGetOptionalData andThen checkExcludedTrader

  def authAndGetOptionalDataAndCheckAlreadyLeft: ActionBuilder[OptionalDataRequest, AnyContent] =
    authAndGetOptionalData andThen checkAlreadyLeft
}

case class DefaultAuthenticatedControllerComponents @Inject()(
                                                               messagesActionBuilder: MessagesActionBuilder,
                                                               actionBuilder: DefaultActionBuilder,
                                                               parsers: PlayBodyParsers,
                                                               messagesApi: MessagesApi,
                                                               langs: Langs,
                                                               fileMimeTypes: FileMimeTypes,
                                                               executionContext: ExecutionContext,
                                                               sessionRepository: SessionRepository,
                                                               identify: IdentifierAction,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               checkExcludedTrader: CheckCancelRequestToLeaveFilter,
                                                               checkAlreadyLeft: CheckAlreadyLeftSchemeFilter,
                                                               checkYourAnswers: CheckYourAnswersFilter
                                                             ) extends AuthenticatedControllerComponents

