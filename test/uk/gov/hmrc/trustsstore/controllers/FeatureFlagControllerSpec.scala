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

package uk.gov.hmrc.trustsstore.controllers

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.trustsstore.models.FeatureFlag.Enabled
import uk.gov.hmrc.trustsstore.models.FeatureFlagName.MLD5
import uk.gov.hmrc.trustsstore.services.FeatureFlagService

import scala.concurrent.Future

class FeatureFlagControllerSpec extends FreeSpec with MustMatchers with MockitoSugar with OptionValues {

  "GET" - {

    "must return a the feature flag from the request" in {

      val mockService = mock[FeatureFlagService]
      when(mockService.get(MLD5)) thenReturn Future.successful(Enabled(MLD5))

      val app = new GuiceApplicationBuilder().overrides(bind[FeatureFlagService].toInstance(mockService)).build()

      running(app) {

        val request = FakeRequest(GET, routes.FeatureFlagController.get(MLD5).url)

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.toJson(Enabled(MLD5))
      }
    }
  }

  "PUT" - {

    "must set a flag and return NO_CONTENT when sent a valid payload" in {

      val mockService = mock[FeatureFlagService]
      when(mockService.set(any(), any())) thenReturn Future.successful(true)

      val app = new GuiceApplicationBuilder()
        .overrides(bind[FeatureFlagService].toInstance(mockService)).build()

      running(app) {

        val request =
          FakeRequest(PUT, routes.FeatureFlagController.put(MLD5).url)
            .withJsonBody(JsBoolean(true))

        val result = route(app, request).value

        status(result) mustEqual NO_CONTENT
        verify(mockService, times(1)).set(MLD5, enabled = true)
      }
    }

    "must return BAD_REQUEST when sent a valid payload" in {

      val mockService = mock[FeatureFlagService]
      when(mockService.set(any(), any())) thenReturn Future.successful(true)

      val app = new GuiceApplicationBuilder().overrides(bind[FeatureFlagService].toInstance(mockService)).build()

      running(app) {

        val request =
          FakeRequest(PUT, routes.FeatureFlagController.put(MLD5).url)
            .withJsonBody(JsString("foo"))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
