/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.models

import play.api.libs.json.Json
import reactivemongo.api.commands.WriteError
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.claim_a_trust.repository.StorageErrors

class StorageErrorsSpec extends BaseSpec {

  "StorageErrors" - {
    "must be able to provide a json object of write errors" in {
      val storageErrors = StorageErrors(
        Seq(
          WriteError(index = 0, code = 100, "some mongo write error!"),
          WriteError(index = 3, code = 20, "also this write error!"),
          WriteError(index = 0, code = 200, "a second write error on index 0!"),
          WriteError(index = 1, code = 100, "another mongo write error!"),
          WriteError(index = 2, code = 50, "a different mongo write error!")
        )
      )

      val expectedJson = Json.obj("errors" ->
        Json.obj(
          "Index 0" ->
            Json.arr("some mongo write error!", "a second write error on index 0!"),
          "Index 1" ->
            Json.arr("another mongo write error!"),
          "Index 2" ->
            Json.arr("a different mongo write error!"),
          "Index 3" ->
            Json.arr("also this write error!")
        )
      )

      storageErrors.toJson mustBe expectedJson
    }
  }
}
