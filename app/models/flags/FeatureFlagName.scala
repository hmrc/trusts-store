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

import play.api.libs.json._
import play.api.mvc.PathBindable

sealed trait FeatureFlagName {
  val asString: String
}

object FeatureFlagName {

  case object `5MLD` extends FeatureFlagName {
    override val asString: String = "5mld"
  }

  case object NonTaxableAccessCode extends FeatureFlagName {
    override val asString: String = "non-taxable.access-code"
  }

  implicit val reads: Reads[FeatureFlagName] = Reads {
    case JsString(`5MLD`.asString)               => JsSuccess(`5MLD`)
    case JsString(NonTaxableAccessCode.asString) => JsSuccess(NonTaxableAccessCode)
    case _                                       => JsError("Unrecognised feature flag name")
  }

  implicit val writes: Writes[FeatureFlagName] =
    Writes(value => JsString(value.asString))

  implicit val pathBinder: PathBindable[FeatureFlagName] = new PathBindable[FeatureFlagName] {
    override def bind(key: String, value: String): Either[String, FeatureFlagName] =
      JsString(value).validate[FeatureFlagName] match {
        case JsSuccess(name, _) => Right(name)
        case _                  => Left("invalid feature flag name")
      }
    override def unbind(key: String, value: FeatureFlagName): String               =
      value.asString
  }

}
