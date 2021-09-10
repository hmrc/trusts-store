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

import config.AppConfig
import models.flags.FeatureFlag.Disabled
import models.flags.FeatureFlagName.{NonTaxableAccessCode, `5MLD`}
import models.flags.{FeatureFlag, FeatureFlagName}
import repositories.FeaturesRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FeatureFlagService @Inject()(
                                    featuresRepository: FeaturesRepository,
                                    config: AppConfig
                                  )(implicit ec: ExecutionContext) {

  def get(name: FeatureFlagName): Future[FeatureFlag] = {
    config.getFeature(name) match {
      case Some(flag) =>
        Future.successful(FeatureFlag(name, flag))
      case _ =>
        getAll.map { flags =>
          lazy val defaultFlag = Disabled(name)
          flags.find(_.name == name).getOrElse(defaultFlag)
        }
    }
  }

  def set(flagName: FeatureFlagName, enabled: Boolean): Future[Boolean] = {
    getAll.flatMap { currentFlags =>
      val updatedFlags = currentFlags.filterNot(_.name == flagName) :+ FeatureFlag(flagName, enabled)
      featuresRepository.setFeatureFlags(updatedFlags)
    }
  }

  private def getAll: Future[Seq[FeatureFlag]] = {

    def addDefaultFlagsIfNotPresent(storedFlags: Seq[FeatureFlag]): Seq[FeatureFlag] = {
      val defaultFlags: Seq[FeatureFlag] = Seq(
        Disabled(`5MLD`),
        Disabled(NonTaxableAccessCode)
      )

      val missingFlags = defaultFlags.filterNot(defaultFlag => storedFlags.exists(_.name == defaultFlag.name))
      storedFlags ++ missingFlags
    }

    featuresRepository.getFeatureFlags.map(addDefaultFlagsIfNotPresent)
  }

}
