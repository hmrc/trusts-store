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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.TrustClaim
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

      val trustClaim = TrustClaim(utr = "0123456789", managedByAgent = true)

      when(service.get(any())).thenReturn(Future.successful(Some(trustClaim)))

      val result = route(application, request).value

      status(result) mustBe Status.OK
    }

    "should return NOT_FOUND if there is no TrustClaim for the internal id" in {
      val request = FakeRequest(GET, routes.ClaimedTrustsController.get().url)

      when(service.get(any())).thenReturn(Future.successful(None))

      val result = route(application, request).value

      status(result) mustBe Status.NOT_FOUND
    }
  }

  "invoking POST /claim" - {
    "should return CREATED" ignore {
      ???
    }
  }

}
