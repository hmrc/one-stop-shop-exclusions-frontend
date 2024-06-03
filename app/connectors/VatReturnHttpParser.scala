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

import logging.Logging
import models.VatReturn
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object VatReturnHttpParser extends Logging {

  type VatReturnMultipleResponse = Seq[VatReturn]

  implicit object VatReturnMultipleReads extends HttpReads[VatReturnMultipleResponse] {

    override def read(method: String, url: String, response: HttpResponse): VatReturnMultipleResponse = {
      response.status match {
        case OK =>
          response.json.validate[Seq[VatReturn]] match {
            case JsSuccess(vatReturns, _) => vatReturns
            case JsError(errors) =>
              logger.warn(s"Failed trying to parse JSON $errors. JSON was ${response.json}")
              Seq.empty
          }

        case NOT_FOUND =>
          logger.warn(s"Received NotFound from vat returns")
          Seq.empty

        case status =>
          val message: String = s"Received unexpected error from vat returns with status: $status"
          logger.warn(message)
          throw new Exception(message)
      }
    }
  }
}
