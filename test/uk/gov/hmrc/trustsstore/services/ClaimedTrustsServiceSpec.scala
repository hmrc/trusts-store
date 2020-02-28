/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.services

import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteError
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim
import uk.gov.hmrc.trustsstore.models.repository.StorageErrors
import uk.gov.hmrc.trustsstore.models.claim_a_trust.responses._
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository

import scala.concurrent.Future

class ClaimedTrustsServiceSpec extends BaseSpec {

  private val repository = mock[ClaimedTrustsRepository]

  lazy val application: Application = applicationBuilder().overrides(
    bind[ClaimedTrustsRepository].toInstance(repository)
  ).build()

  private val service = application.injector.instanceOf[ClaimedTrustsService]

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
  }

  "invoking .get" - {
    "must return a GetClaimFoundResponse from the repository if there is one for the given internal id" in {
      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(repository.get(any())).thenReturn(Future.successful(Some(trustClaim)))

      val result = service.get("matching-internal-id").futureValue

      result mustBe GetClaimFound(trustClaim)
    }

    "must return a GetClaimNotFoundResponse from the repository if there is no claims for the given internal id" in {
      when(repository.get(any())).thenReturn(Future.successful(None))

      val result = service.get("unmatched-internal-id").futureValue

      result mustBe GetClaimNotFound
    }
  }

  "invoking .store" - {
    "must return a StoreSuccessResponse from the repository if the TrustClaim is successfully stored" in {

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(repository.store(any())).thenReturn(Future.successful(Right(trustClaim)))

      val result = service.store(fakeInternalId, Some(fakeUtr), Some(true), None).futureValue

      result mustBe StoreSuccessResponse(trustClaim)
    }

    "must return a StoreSuccessResponse from the repository if the TrustClaim is successfully stored with trustLocked" in {

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(repository.store(any())).thenReturn(Future.successful(Right(trustClaim)))

      val result = service.store(fakeInternalId, Some(fakeUtr), Some(true), Some(true)).futureValue

      result mustBe StoreSuccessResponse(trustClaim)
    }

    "must return a StoreParsingErrorResponse if the request body cannot be parsed into a TrustClaim" in {
      val result = service.store(fakeInternalId, None, None, None).futureValue

      result mustBe StoreParsingError
    }

    "must return a StoreErrorsResponse from the repository if the repository experiences WriteErrors" in {

      val storageErrors = StorageErrors(Seq(WriteError(0, 100, "some mongo write error!"), WriteError(1, 50, "another mongo write error!")))

      when(repository.store(any())).thenReturn(Future.successful(Left(storageErrors)))

      val result = service.store(fakeInternalId, Some(fakeUtr), Some(true), Some(false)).futureValue

      result mustBe StoreErrorsResponse(storageErrors)
    }
  }

}
