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

package services

import javax.inject.Inject
import config.AppConfig
import models.{FeatureFlag, FeatureFlagName}
import models.FeatureFlag.Disabled
import models.FeatureFlagName.MLD5
import repositories.FeaturesRepository

import scala.concurrent.{ExecutionContext, Future}

class FeatureFlagService @Inject()(featuresRepository: FeaturesRepository, config: AppConfig)
                                  (implicit ec: ExecutionContext) {

  private val defaults: Seq[FeatureFlag] = Seq(
    Disabled(MLD5)
  )

  private def addDefaults(fromDb: Seq[FeatureFlag]): Seq[FeatureFlag] = {
    val toAdd = defaults.filterNot(d => fromDb.exists(fdb => fdb.name == d.name))
    fromDb ++ toAdd
  }

  def getAll:Future[Seq[FeatureFlag]] = {
    featuresRepository.getFeatureFlags.map(addDefaults)
  }

  def set(flagName: FeatureFlagName, enabled: Boolean) : Future[Boolean] = {
    getAll.flatMap {
      currentFlags =>
        val newFlags = currentFlags.filterNot(f => f.name == flagName) :+ FeatureFlag(flagName, enabled)

        featuresRepository.setFeatureFlags(newFlags)
    }
  }

  def get(name: FeatureFlagName) : Future[FeatureFlag] = {
    getConfig(name) match {
      case Some(flag) => Future.successful(FeatureFlag(name, flag))
      case _ => getAll.map { flags =>
        flags.find(_.name == name).getOrElse(Disabled(name))
      }
    }
  }

  def getConfig(name: FeatureFlagName) : Option[Boolean] = {
    config.getFeature(name)
  }
}
