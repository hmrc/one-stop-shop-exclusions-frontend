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

package models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpEuRegistrationDetails(
                                      countryOfRegistration: String,
                                      vatNumber: Option[String] = None,
                                      taxIdentificationNumber: Option[String] = None,
                                      fixedEstablishment: Option[Boolean] = None,
                                      tradingName: Option[String] = None,
                                      fixedEstablishmentAddressLine1: Option[String] = None,
                                      fixedEstablishmentAddressLine2: Option[String] = None,
                                      townOrCity: Option[String] = None,
                                      regionOrState: Option[String] = None,
                                      postcode: Option[String] = None
                                    )

object EtmpEuRegistrationDetails {

  implicit val format: OFormat[EtmpEuRegistrationDetails] = Json.format[EtmpEuRegistrationDetails]
}

