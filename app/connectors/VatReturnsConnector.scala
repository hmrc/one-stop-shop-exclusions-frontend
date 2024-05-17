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

import config.FrontendAppConfig
import logging.Logging
import models.VatReturn
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatReturnsConnector @Inject()(
                                     frontendAppConfig: FrontendAppConfig,
                                     httpClient: HttpClient
                                   )(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  private val baseUrl: String = frontendAppConfig.returnsServiceUrl

  def getSubmittedVatReturns: Future[Seq[VatReturn]] = {
    httpClient.GET[Seq[VatReturn]](s"$baseUrl/vat-returns")
  }
}
