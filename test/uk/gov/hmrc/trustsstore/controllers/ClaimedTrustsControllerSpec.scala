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
import uk.gov.hmrc.trustsstore.models.{TrustClaim, _}
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
    "should return OK and a TrustClaim if there is one for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val trustClaim = TrustClaim(internalId = fakeInternalId, utr = fakeUtr, managedByAgent = true)

      when(service.get(any())).thenReturn(Future.successful(GetClaimFoundResponse(trustClaim)))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(trustClaim)
    }

    "should return NOT_FOUND if there is no TrustClaim for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val responseError = Json.obj("errors" -> "No TrustClaim was found for the given for this authenticated internalId")

      when(service.get(any())).thenReturn(Future.successful(GetClaimNotFoundResponse(responseError)))

      val result = route(application, request).value

      status(result) mustBe Status.NOT_FOUND
      contentAsJson(result) mustBe responseError
    }
  }

  "invoking POST /claim" - {
    "should return CREATED and the stored TrustClaim if the service returns a StoreSuccessResponse" in {
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

    "should return BAD_REQUEST and an error response if the service returns a StoreParsingErrorResponse" in {
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

    "should return INTERNAL_SERVER_ERROR and an error response if the service returns a StoreErrorsResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(Json.obj(
          "some-incorrect-key" -> "some-incorrect-value"
        ))

      val writeErrors = Seq(WriteError(0, 0, "some mongo write error!"), WriteError(1, 0, "another mongo write error!"))

      when(service.store(any(), any(), any())).thenReturn(Future.successful(StoreErrorsResponse(writeErrors)))

      val result = route(application, request).value

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.obj("errors" -> Json.arr("some mongo write error!", "another mongo write error!"))
    }
  }

}
