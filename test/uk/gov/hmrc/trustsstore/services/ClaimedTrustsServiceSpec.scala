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

package uk.gov.hmrc.trustsstore.services

import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteError
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.TrustClaim
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository
import uk.gov.hmrc.trustsstore.models._

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
    "should return a GetClaimFoundResponse from the repository if there is one for the given internal id" in {
      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(repository.get(any())).thenReturn(Future.successful(Some(trustClaim)))

      val result = service.get("matching-internal-id").futureValue

      result mustBe GetClaimFoundResponse(trustClaim)
    }

    "should return a GetClaimNotFoundResponse from the repository if there is no claims for the given internal id" in {
      val responseError = Json.obj("errors" -> "No TrustClaim was found for the given for this authenticated internalId")

      when(repository.get(any())).thenReturn(Future.successful(None))

      val result = service.get("unmatched-internal-id").futureValue

      result mustBe GetClaimNotFoundResponse(responseError)
    }
  }

  "invoking POST /claim" - {
    "should return a StoreSuccessResponse from the repository if the TrustClaim is successfully stored" in {

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(repository.store(any())).thenReturn(Future.successful(Right(trustClaim)))

      val result = service.store(fakeInternalId, Some(fakeUtr), Some(true)).futureValue

      result mustBe StoreSuccessResponse(trustClaim)
    }

    "should return a StoreParsingErrorResponse if the request body cannot be parsed into a TrustClaim" in {
      val responseError = Json.obj("errors" ->  "Unable to parse request body into a TrustClaim")

      val result = service.store(fakeInternalId, None, None).futureValue

      result mustBe StoreParsingErrorResponse(responseError)
    }

    "should return a StoreErrorsResponse from the repository if the repository experiences WriteErrors" in {

      val writeErrors = Seq(WriteError(0, 0, "some mongo write error!"), WriteError(1, 0, "another mongo write error!"))

      when(repository.store(any())).thenReturn(Future.successful(Left(writeErrors)))

      val result = service.store(fakeInternalId, Some(fakeUtr), Some(true)).futureValue

      result mustBe StoreErrorsResponse(writeErrors)
    }
  }

}
