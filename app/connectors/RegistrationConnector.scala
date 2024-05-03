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

package connectors

import config.Service
import connectors.RegistrationHttpParser._
import models.registration.Registration
import models.requests.EtmpAmendRegistrationRequest
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClient: HttpClient)
                                     (implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.one-stop-shop-registration")

  def get()(implicit hc: HeaderCarrier): Future[Registration] = {
    httpClient.GET[Registration](s"$baseUrl/registration")
  }

  def amend(registrationRequest: EtmpAmendRegistrationRequest)(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] = {
    httpClient.POST[EtmpAmendRegistrationRequest, AmendRegistrationResultResponse](s"$baseUrl/amend", registrationRequest)
  }
}