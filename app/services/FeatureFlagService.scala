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

import javax.inject.Inject
import config.AppConfig
import models.{FeatureFlag, FeatureFlagName}
import models.FeatureFlag.Disabled
import models.FeatureFlagName.`5MLD`
import repositories.FeaturesRepository

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
    featuresRepository.getFeatureFlags.map(addDefaultFlagsIfNotPresent)
  }

  private def addDefaultFlagsIfNotPresent(storedFlags: Seq[FeatureFlag]): Seq[FeatureFlag] = {
    val defaultFlags: Seq[FeatureFlag] = Seq(
      Disabled(`5MLD`)
    )

    val missingFlags = defaultFlags.filterNot(defaultFlag => storedFlags.exists(_.name == defaultFlag.name))
    storedFlags ++ missingFlags
  }

}
