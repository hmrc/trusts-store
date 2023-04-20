/*
 * Copyright 2023 HM Revenue & Customs
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

package models.flags

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait FeatureFlag {
  def name: FeatureFlagName
  def isEnabled: Boolean
}

object FeatureFlag {

  case class Enabled(name: FeatureFlagName) extends FeatureFlag {
    override val isEnabled = true
  }

  case class Disabled(name: FeatureFlagName) extends FeatureFlag {
    override val isEnabled = false
  }

  def apply(name: FeatureFlagName, enabled: Boolean): FeatureFlag =
    if (enabled) {
      Enabled(name)
    } else {
      Disabled(name)
    }

  implicit val reads: Reads[FeatureFlag] =
    (__ \ "isEnabled").read[Boolean].flatMap {
      case true  => (__ \ "name").read[FeatureFlagName].map(Enabled(_).asInstanceOf[FeatureFlag])
      case false => (__ \ "name").read[FeatureFlagName].map(Disabled(_).asInstanceOf[FeatureFlag])
    }

  implicit val writes: Writes[FeatureFlag] = (
    (__ \ "name").write[FeatureFlagName] and
      (__ \ "isEnabled").write[Boolean]
  ).apply(ff => (ff.name, ff.isEnabled))

}
