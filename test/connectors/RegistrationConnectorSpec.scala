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
import data.RegistrationData
import generators.Generators
import models.registration.Registration
import models.responses.UnexpectedResponseStatus
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationConnectorSpec
  extends SpecBase
    with WireMockHelper
    with ScalaCheckPropertyChecks
    with Generators
    with RegistrationData {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application = {
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-registration.port" -> server.port)
      .build()
  }

  ".get" - {
    val url = s"/one-stop-shop-registration/registration"

    "must return a registration when the server provides one" in {

      val app = application

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val registration = arbitrary[Registration].sample.value

        val responseBody = Json.toJson(registration).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.get().futureValue

        result mustEqual registration
      }
    }

  }

  ".amend" - {
    val url = s"/one-stop-shop-registration/amend"

    "must return Right when a new registration is created on the backend" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(ok()))

        val result = connector.amend(etmpAmendRegistrationRequest).futureValue

        result mustBe Right(())
      }
    }

    "must return Left(UnexpectedResponseStatus) when the backend returns UnexpectedResponseStatus" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(123)))

        val result = connector.amend(etmpAmendRegistrationRequest).futureValue

        result mustBe Left(UnexpectedResponseStatus(123, "Unexpected amend response, status 123 returned"))
      }
    }
  }

}