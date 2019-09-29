/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.models.claim_a_trust.responses

import play.api.libs.json.JsObject
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim
import uk.gov.hmrc.trustsstore.models.claim_a_trust.repository.StorageErrors

trait ClaimedTrustResponse

case class GetClaimFoundResponse(foundTrustClaim: TrustClaim) extends ClaimedTrustResponse
case class GetClaimNotFoundResponse(error: JsObject) extends ClaimedTrustResponse

case class StoreErrorsResponse(errors: StorageErrors) extends ClaimedTrustResponse
case class StoreSuccessResponse(storedTrustClaim: TrustClaim) extends ClaimedTrustResponse
case class StoreParsingErrorResponse(error: JsObject) extends ClaimedTrustResponse