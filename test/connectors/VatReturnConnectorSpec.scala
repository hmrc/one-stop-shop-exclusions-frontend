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
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.VatReturn
import org.scalacheck.Gen
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier


class VatReturnConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application = applicationBuilder()
    .configure("microservice.services.one-stop-shop-returns.port" -> server.port())
    .build()

  "VatReturnConnector" - {

    ".getSubmittedVatReturns" - {

      val url: String = "/one-stop-shop-returns/vat-returns"

      "must return a Seq VatReturn when connector returns a successful payload" in {

        val vatReturns: Seq[VatReturn] = Gen.listOfN(4, arbitraryVatReturn.arbitrary).sample.value

        running(application) {
          val connector = application.injector.instanceOf[VatReturnConnector]

          val responseBody = Json.toJson(vatReturns).toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseBody)
          ))

          val result = connector.getSubmittedVatReturns().futureValue

          result mustBe vatReturns
        }
      }

      "must return Seq.empty when JSON cannot be parsed correctly" in {

        running(application) {
          val connector = application.injector.instanceOf[VatReturnConnector]

          val responseBody: String = """{ "foo": "bar" }"""

          server.stubFor(get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(responseBody)
          ))

          val result = connector.getSubmittedVatReturns().futureValue

          result mustBe Seq.empty
        }
      }

      "must return Seq.empty when connector returns a NotFound" in {

        running(application) {
          val connector = application.injector.instanceOf[VatReturnConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          ))

          val result = connector.getSubmittedVatReturns().futureValue

          result mustBe Seq.empty
        }
      }

      "must throw an Exception when connector returns an error" in {

        val error: Int = INTERNAL_SERVER_ERROR

        running(application) {
          val connector = application.injector.instanceOf[VatReturnConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(error)
          ))

          whenReady(connector.getSubmittedVatReturns().failed) { exp =>
            exp mustBe a[Exception]
            exp.getMessage mustBe s"Received unexpected error from vat returns with status: $error"
          }
        }
      }
    }
  }
}
