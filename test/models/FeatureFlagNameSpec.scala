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

package models

import base.BaseSpec
import models.FeatureFlagName.{NonTaxable, `5MLD`}
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class FeatureFlagNameSpec extends BaseSpec {

  "FeatureFlagName" - {

    "must de-serialise and serialise" - {

      "5MLD feature flag" in {
        val json = JsString("5mld")
        val result = json.validate[FeatureFlagName]
        result mustBe JsSuccess(`5MLD`)
        Json.toJson(result.get) mustBe json
      }

      "NonTaxable feature flag" in {
        val json = JsString("non-taxable")
        val result = json.validate[FeatureFlagName]
        result mustBe JsSuccess(NonTaxable)
        Json.toJson(result.get) mustBe json
      }

      "unknown feature flag" in {
        val json = JsString("unknown feature")
        val result = json.validate[FeatureFlagName]
        result mustBe JsError("Unrecognised feature flag name")
      }
    }
  }
}
