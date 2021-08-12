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

package models.repository

import base.BaseSpec
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteError

class StorageErrorsSpec extends BaseSpec {

  "StorageErrors" - {
    "must be able to provide a json object of write errors" in {

      val storageErrorsJson = StorageErrors(
        Seq(
          WriteError(index = 0, code = 50, "some mongo write error!"),
          WriteError(index = 1, code = 100, "another mongo write error!"),
          WriteError(index = 2, code = 20, "a different mongo write error!"),
          WriteError(index = 3, code = 100, "also this write error!") ,
          WriteError(index = 3, code = 200, "a second write error on index 3!")
        )
      ).toJson

      val expectedJson = Json.parse(
        """
          |[
          |  { "index 2": [{ "code": 20, "message": "a different mongo write error!" }] },
          |  { "index 1": [{ "code": 100, "message": "another mongo write error!" }] },
          |  {
          |    "index 3": [
          |      { "code": 100, "message": "also this write error!" },
          |      { "code": 200, "message": "a second write error on index 3!" }
          |    ]
          |  },
          |  { "index 0": [{ "code": 50, "message": "some mongo write error!" }] }
          |]
        """.stripMargin
      )

      storageErrorsJson mustBe expectedJson
    }
  }
}
