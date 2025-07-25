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
import connectors.VatReturnHttpParser._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatReturnConnector @Inject()(
                                    frontendAppConfig: FrontendAppConfig,
                                    httpClientV2: HttpClientV2
                                  )(implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl: String = frontendAppConfig.returnsServiceUrl

  def getSubmittedVatReturns()(implicit hc: HeaderCarrier): Future[VatReturnMultipleResponse] = {
    httpClientV2.get(url"$baseUrl/vat-returns").execute[VatReturnMultipleResponse]
  }
}
