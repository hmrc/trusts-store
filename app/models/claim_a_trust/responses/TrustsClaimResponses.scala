/*
 * Copyright 2025 HM Revenue & Customs
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

package models.claim_a_trust.responses

import models.claim_a_trust.TrustClaim

trait ClaimedTrustResponse

object ClaimedTrustResponse {
  val CLAIM_TRUST_UNABLE_TO_LOCATE = "unable to locate a TrustClaim for the given requests internalId"
  val CLAIM_TRUST_UNABLE_TO_PARSE  = "Unable to parse request body into a TrustClaim"
}

case class GetClaimFound(foundTrustClaim: TrustClaim) extends ClaimedTrustResponse
case object GetClaimNotFound extends ClaimedTrustResponse

case class StoreSuccessResponse(storedTrustClaim: TrustClaim) extends ClaimedTrustResponse
case object StoreParsingError extends ClaimedTrustResponse
