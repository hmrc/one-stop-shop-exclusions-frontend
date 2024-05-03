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

package models.requests

import models.etmp._
import play.api.libs.json.{Json, OFormat}

case class EtmpAmendRegistrationRequest(
                                         administration: EtmpAdministration,
                                         changeLog: EtmpAmendRegistrationChangeLog,
                                         exclusionDetails: Option[EtmpExclusionDetails],
                                         customerIdentification: EtmpAmendCustomerIdentification,
                                         tradingNames: Seq[EtmpTradingNames],
                                         schemeDetails: EtmpSchemeDetails,
                                         bankDetails: BankDetails,
                                       )

object EtmpAmendRegistrationRequest {
  implicit val format: OFormat[EtmpAmendRegistrationRequest] = Json.format[EtmpAmendRegistrationRequest]
}