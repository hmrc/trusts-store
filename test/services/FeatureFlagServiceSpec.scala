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

package services

import base.BaseSpec
import config.AppConfig
import models.flags.FeatureFlag
import models.flags.FeatureFlag.Enabled
import models.flags.FeatureFlagName.`5MLD`
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.Application
import play.api.inject.bind
import repositories.FeaturesRepository

import scala.concurrent.Future

class FeatureFlagServiceSpec extends BaseSpec {

  private val mockRepository = mock[FeaturesRepository]
  private val mockConfig = mock[AppConfig]

  lazy val application: Application = applicationBuilder()
    .overrides(
      bind[FeaturesRepository].toInstance(mockRepository),
      bind[AppConfig].toInstance(mockConfig)
    ).build()

  private val service = application.injector.instanceOf[FeatureFlagService]

  private val feature = `5MLD`
  private val featureEnabled = FeatureFlag(feature, enabled = true)
  private val featureDisabled = FeatureFlag(feature, enabled = false)

  override def beforeEach(): Unit = {
    reset(mockRepository)
  }

  "FeatureFlagService" - {

    ".get" - {

      "must return flag from config if it exists" in {
        when(mockConfig.getFeature(any())).thenReturn(Some(true))

        val result = service.get(feature).futureValue

        result mustBe featureEnabled
      }

      "must return flag from repository if it exists" in {
        when(mockConfig.getFeature(any())).thenReturn(None)

        when(mockRepository.getFeatureFlags).thenReturn(Future.successful(Seq(featureEnabled)))

        val result = service.get(feature).futureValue

        result mustBe featureEnabled
      }

      "must return disabled by default if flag does not exist" in {
        when(mockConfig.getFeature(any())).thenReturn(None)

        when(mockRepository.getFeatureFlags).thenReturn(Future.successful(Nil))

        val result = service.get(feature).futureValue

        result mustBe featureDisabled
      }
    }

    ".set" - {
      "must update flags with new flag" - {

        "when flag exists" in {
          when(mockRepository.getFeatureFlags).thenReturn(Future.successful(Seq(featureEnabled)))
          when(mockRepository.setFeatureFlags(any())).thenReturn(Future.successful(true))

          val result = service.set(feature, enabled = true).futureValue

          result mustBe true

          verify(mockRepository).setFeatureFlags(Seq(Enabled(feature)))
        }

        "when flag does not exist" in {
          when(mockRepository.getFeatureFlags).thenReturn(Future.successful(Nil))
          when(mockRepository.setFeatureFlags(any())).thenReturn(Future.successful(true))

          val result = service.set(feature, enabled = true).futureValue

          result mustBe true

          verify(mockRepository).setFeatureFlags(Seq(Enabled(feature)))
        }
      }
    }
  }
}
