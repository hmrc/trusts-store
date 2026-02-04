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

package controllers

import base.BaseSpec
import models.claim_a_trust.TrustClaim
import models.claim_a_trust.responses._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ClaimedTrustsService

import scala.concurrent.Future

class ClaimedTrustsControllerSpec extends BaseSpec {

  private val service: ClaimedTrustsService = mock[ClaimedTrustsService]

  lazy val application: Application = applicationBuilder()
    .overrides(
      bind[ClaimedTrustsService].toInstance(service)
    )
    .build()

  override def beforeEach(): Unit =
    Mockito.reset(service)

  "invoking GET /claim" should {
    "return OK and a TrustClaim if there is one for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val trustClaim = TrustClaim(internalId = fakeInternalId, identifier = fakeUtr, managedByAgent = true)

      when(service.get(any())).thenReturn(Future.successful(GetClaimFound(trustClaim)))

      val result = route(application, request).value

      status(result)        mustBe Status.OK
      contentAsJson(result) mustBe trustClaim.toResponse
    }

    "return NOT_FOUND if there is no TrustClaim for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      val expectedJson = Json.parse(
        """
          |{
          |  "status": 404,
          |  "message": "unable to locate a TrustClaim for the given requests internalId"
          |}
        """.stripMargin
      )

      when(service.get(any())).thenReturn(Future.successful(GetClaimNotFound))

      val result = route(application, request).value

      status(result)        mustBe Status.NOT_FOUND
      contentAsJson(result) mustBe expectedJson
    }
  }

  "invoking POST /claim" should {

    "return CREATED and the stored TrustClaim if the service returns a StoreSuccessResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(
          Json.obj(
            "id"             -> "0123456789",
            "managedByAgent" -> true
          )
        )

      val trustClaim = TrustClaim(internalId = fakeInternalId, identifier = fakeUtr, managedByAgent = true)

      when(service.store(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful(StoreSuccessResponse(trustClaim)))

      val result = route(application, request).value

      status(result)        mustBe Status.CREATED
      contentAsJson(result) mustBe trustClaim.toResponse
    }

    "return BAD_REQUEST and an error response if the service returns a StoreParsingErrorResponse" in {
      val request = FakeRequest(POST, routes.ClaimedTrustsController.store().url)
        .withJsonBody(
          Json.obj(
            "some-incorrect-key" -> "some-incorrect-value"
          )
        )

      val expectedJson = Json.parse(
        """
          |{
          |  "status": 400,
          |  "message": "Unable to parse request body into a TrustClaim"
          |}
        """.stripMargin
      )

      when(service.store(any(), any(), any(), any())(any())).thenReturn(Future.successful(StoreParsingError))

      val result = route(application, request).value

      status(result)        mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe expectedJson
    }
  }

}
