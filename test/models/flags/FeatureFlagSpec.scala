/*
 * Copyright 2026 HM Revenue & Customs
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

import base.BaseSpec
import models.flags.FeatureFlag.{Disabled, Enabled}
import models.flags.FeatureFlagName.{NonTaxableAccessCode, `5MLD`}
import play.api.libs.json.Json

class FeatureFlagSpec extends BaseSpec {

  "FeatureFlag" should {

    "de-serialise and serialise" should {

      "5MLD" should {

        "enabled" in {
          val json   = Json.parse(
            """
              |{
              |  "name": "5mld",
              |  "isEnabled": true
              |}
              |""".stripMargin
          )
          val result = json.as[FeatureFlag]
          result              mustBe Enabled(`5MLD`)
          Json.toJson(result) mustBe json
        }

        "disabled" in {
          val json   = Json.parse(
            """
              |{
              |  "name": "5mld",
              |  "isEnabled": false
              |}
              |""".stripMargin
          )
          val result = json.as[FeatureFlag]
          result              mustBe Disabled(`5MLD`)
          Json.toJson(result) mustBe json
        }
      }

      "NonTaxableAccessCode" should {

        "enabled" in {
          val json   = Json.parse(
            """
              |{
              |  "name": "non-taxable.access-code",
              |  "isEnabled": true
              |}
              |""".stripMargin
          )
          val result = json.as[FeatureFlag]
          result              mustBe Enabled(NonTaxableAccessCode)
          Json.toJson(result) mustBe json
        }

        "disabled" in {
          val json   = Json.parse(
            """
              |{
              |  "name": "non-taxable.access-code",
              |  "isEnabled": false
              |}
              |""".stripMargin
          )
          val result = json.as[FeatureFlag]
          result              mustBe Disabled(NonTaxableAccessCode)
          Json.toJson(result) mustBe json
        }
      }
    }
  }

}
