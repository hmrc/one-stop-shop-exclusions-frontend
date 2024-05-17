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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.VatReturn
import org.scalacheck.Gen
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers.running


class VatReturnsConnectorSpec extends SpecBase with WireMockHelper {

  private def application = applicationBuilder()
    .configure("microservice.services.one-stop-shop-returns.port" -> server.port())
    .build()

  "VatReturnsConnector" - {

    ".getSubmittedVatReturns" - {

      val url: String = "/one-stop-shop-returns/vat-returns"

      "must return a Seq VatReturn when connector returns a successful payload" in {

        val vatReturns: Seq[VatReturn] = Gen.listOfN(4, arbitraryVatReturn.arbitrary).sample.value

        running(application) {
          val connector = application.injector.instanceOf[VatReturnsConnector]

          val responseBody = Json.toJson(vatReturns).toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseBody)
          ))

          val result = connector.getSubmittedVatReturns.futureValue

          result mustBe vatReturns
        }
      }
    }
  }
}
