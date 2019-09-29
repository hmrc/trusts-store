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

package uk.gov.hmrc.trustsstore.controllers

import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteError
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim
import uk.gov.hmrc.trustsstore.models.claim_a_trust.repository.StorageErrors
import uk.gov.hmrc.trustsstore.models.claim_a_trust.responses._
import uk.gov.hmrc.trustsstore.services.ClaimedTrustsService

import scala.concurrent.Future


class ClaimedTrustsControllerSpec extends BaseSpec {


  private val service: ClaimedTrustsService = mock[ClaimedTrustsService]

  lazy val application: Application = applicationBuilder().overrides(
    bind[ClaimedTrustsService].toInstance(service)
  ).build()

  override def beforeEach() = {
    Mockito.reset(service)
  }

  "invoking GET /claim" - {
    "must return OK and a TrustClaim if there is one for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(service.get(any())).thenReturn(Future.successful(GetClaimFoundResponse(trustClaim)))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(trustClaim)
    }

    "must return NOT_FOUND if there is no TrustClaim for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val responseError = Json.obj("errors" -> "No TrustClaim was found for the given for this authenticated internalId")

      when(service.get(any())).thenReturn(Future.successful(GetClaimNotFoundResponse(responseError)))

      val result = route(application, request).value

      status(result) mustBe Status.NOT_FOUND
      contentAsJson(result) mustBe responseError
    }
  }

  "invoking POST /claim" - {
    "must return CREATED and the stored TrustClaim if the service returns a StoreSuccessResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(Json.obj(
          "utr" -> "0123456789",
          "managedByAgent" -> true
        ))

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(service.store(any(), any(), any())).thenReturn(Future.successful(StoreSuccessResponse(trustClaim)))

      val result = route(application, request).value

      status(result) mustBe Status.CREATED
      contentAsJson(result) mustBe Json.toJson(trustClaim)
    }

    "must return BAD_REQUEST and an error response if the service returns a StoreParsingErrorResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(Json.obj(
          "some-incorrect-key" -> "some-incorrect-value"
        ))

      val responseError = Json.obj("errors" ->  "Unable to parse request body into a TrustClaim")

      when(service.store(any(), any(), any())).thenReturn(Future.successful(StoreParsingErrorResponse(responseError)))

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe responseError
    }

    "must return INTERNAL_SERVER_ERROR and an error response if the service returns a StoreErrorsResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(Json.obj(
          "some-incorrect-key" -> "some-incorrect-value"
        ))

      val storageErrors = StorageErrors(
        Seq(
          WriteError(index = 0, code = 100, "some mongo write error!"),
          WriteError(index = 1, code = 100, "another mongo write error!"),
          WriteError(index = 0, code = 200, "a different mongo write error!")
        )
      )

      val expectedJson = Json.obj("errors" ->
        Json.obj(
          "Index 0" ->
          Json.arr("some mongo write error!", "a different mongo write error!"),
          "Index 1" ->
          Json.arr("another mongo write error!")
        )
      )

      when(service.store(any(), any(), any())).thenReturn(Future.successful(StoreErrorsResponse(storageErrors)))

      val result = route(application, request).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe expectedJson
    }
  }

}
