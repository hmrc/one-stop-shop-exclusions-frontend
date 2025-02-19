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

package models.amend

import org.scalatest.matchers.must.Matchers
import models.exclusions.ExclusionReason
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

import java.time.LocalDate

class EtmpExclusionDetailsSpec extends AnyWordSpec with Matchers {

  "EtmpExclusionDetails" must {

    "serialize to JSON correctly" in {
      val exclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,  // Use one of the ExclusionReason values
        movePOBDate = Some(LocalDate.of(2025, 1, 1)),
        issuedBy = Some("John Doe"),
        vatNumber = Some("GB123456789")
      )

      val expectedJson: JsObject = Json.obj(
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> "1",  // This will correspond to NoLongerSupplies
        "movePOBDate" -> "2025-01-01",
        "issuedBy" -> "John Doe",
        "vatNumber" -> "GB123456789"
      )

      Json.toJson(exclusionDetails) mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> "1",  // Corresponds to NoLongerSupplies
        "movePOBDate" -> "2025-01-01",
        "issuedBy" -> "John Doe",
        "vatNumber" -> "GB123456789"
      )

      val expectedExclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,
        movePOBDate = Some(LocalDate.of(2025, 1, 1)),
        issuedBy = Some("John Doe"),
        vatNumber = Some("GB123456789")
      )

      json.as[EtmpExclusionDetails] mustEqual expectedExclusionDetails
    }

    "deserialize from JSON correctly with missing optional fields" in {
      val json = Json.obj(
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> "1"  // Corresponds to NoLongerSupplies
      )

      val expectedExclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,
        movePOBDate = None,
        issuedBy = None,
        vatNumber = None
      )

      json.as[EtmpExclusionDetails] mustEqual expectedExclusionDetails
    }

    "use default partyType value when not provided" in {
      val exclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,
        movePOBDate = Some(LocalDate.of(2025, 1, 1)),
        issuedBy = Some("John Doe"),
        vatNumber = Some("GB123456789")
      )

      exclusionDetails.partyType mustEqual "NETP"
    }

    "handle missing movePOBDate, issuedBy, and vatNumber correctly" in {
      val exclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,
        movePOBDate = None,
        issuedBy = None,
        vatNumber = None
      )

      exclusionDetails.movePOBDate mustBe None
      exclusionDetails.issuedBy mustBe None
      exclusionDetails.vatNumber mustBe None
    }

    "handle missing VAT number when deserializing" in {
      val json = Json.obj(
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> "1",  // Corresponds to NoLongerSupplies
        "movePOBDate" -> "2025-01-01",
        "issuedBy" -> "John Doe"
      )

      val expectedExclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2024, 12, 5),
        exclusionReason = ExclusionReason.NoLongerSupplies,
        movePOBDate = Some(LocalDate.of(2025, 1, 1)),
        issuedBy = Some("John Doe"),
        vatNumber = None
      )

      json.as[EtmpExclusionDetails] mustEqual expectedExclusionDetails
    }

    "handle missing values when deserializing" in {

      val json: JsObject = Json.obj()

      json.validate[EtmpExclusionDetails] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json: JsObject = Json.obj(
        "partyType" -> 12345,
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> "1", // This will correspond to NoLongerSupplies
        "movePOBDate" -> "2025-01-01",
        "issuedBy" -> "John Doe",
        "vatNumber" -> "GB123456789"
      )

      json.validate[EtmpExclusionDetails] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json: JsObject = Json.obj(
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2024-12-05",
        "exclusionReason" -> JsNull,
        "movePOBDate" -> "2025-01-01",
        "issuedBy" -> "John Doe",
        "vatNumber" -> "GB123456789"
      )

      json.validate[EtmpExclusionDetails] mustBe a[JsError]

    }
  }
}