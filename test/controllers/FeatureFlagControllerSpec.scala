/*
 * Copyright 2021 HM Revenue & Customs
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
import models.flags.FeatureFlag
import models.flags.FeatureFlag.Enabled
import models.flags.FeatureFlagName.NonTaxableAccessCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import play.api.libs.json.{JsBoolean, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FeatureFlagService

import scala.concurrent.Future

class FeatureFlagControllerSpec extends BaseSpec {

  "GET" - {

    "must return the feature flag from the request when" - {
      "config for the flag exists" in {

        val app = applicationBuilder()
          .configure((s"features.${NonTaxableAccessCode.asString}", true))
          .build()

        running(app) {

          val request = FakeRequest(GET, routes.FeatureFlagController.get(NonTaxableAccessCode).url)

          val result = route(app, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson[FeatureFlag](Enabled(NonTaxableAccessCode))
        }
      }

      "config for the flag does not exist" in {

        val mockService = mock[FeatureFlagService]
        when(mockService.get(NonTaxableAccessCode)) thenReturn Future.successful(Enabled(NonTaxableAccessCode))

        val app = applicationBuilder()
          .overrides(bind[FeatureFlagService].toInstance(mockService))
          .build()

        running(app) {

          val request = FakeRequest(GET, routes.FeatureFlagController.get(NonTaxableAccessCode).url)

          val result = route(app, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson[FeatureFlag](Enabled(NonTaxableAccessCode))
        }
      }
    }
  }

  "PUT" - {

    "must set a flag and return NO_CONTENT when sent a valid payload" in {

      val mockService = mock[FeatureFlagService]
      when(mockService.set(any(), any())) thenReturn Future.successful(true)

      val app = applicationBuilder()
        .overrides(bind[FeatureFlagService].toInstance(mockService))
        .build()

      running(app) {

        val request = FakeRequest(PUT, routes.FeatureFlagController.put(NonTaxableAccessCode).url)
          .withJsonBody(JsBoolean(true))

        val result = route(app, request).value

        status(result) mustEqual NO_CONTENT
        verify(mockService, times(1)).set(NonTaxableAccessCode, enabled = true)
      }
    }

    "must return BAD_REQUEST when sent a valid payload" in {

      val mockService = mock[FeatureFlagService]
      when(mockService.set(any(), any())) thenReturn Future.successful(true)

      val app = applicationBuilder()
        .overrides(bind[FeatureFlagService].toInstance(mockService))
        .build()

      running(app) {

        val request = FakeRequest(PUT, routes.FeatureFlagController.put(NonTaxableAccessCode).url)
          .withJsonBody(JsString("foo"))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
