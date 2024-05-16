/*
 * Copyright 2023 HM Revenue & Customs
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

package models.audit

import models.{Enumerable, WithName}

sealed trait ExclusionAuditType {
  def auditType: String

  def transactionName: String
}

object ExclusionAuditType extends Enumerable.Implicits {
  case object ExclusionRequestSubmitted extends WithName("ExclusionRequestSubmitted") with ExclusionAuditType {
    override val auditType: String = "ExclusionRequestSubmitted"
    override val transactionName: String = "exclusion-request-submitted"
  }

  case object ReversalRequestSubmitted extends WithName("ReversalRequestSubmitted") with ExclusionAuditType {
    override val auditType: String = "ReversalRequestSubmitted"
    override val transactionName: String = "reversal-request-submitted"
  }
}
