/*
 * Copyright 2024 HM Revenue & Customs
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

package models.responses

import base.BaseSpec
import models.claim_a_trust.responses.ClaimedTrustResponse._
import models.responses.ErrorResponse._
import play.api.http.Status._
import play.api.libs.json.Json

class ErrorResponseSpec extends BaseSpec {

  "ErrorResponse" should {
    "be able to provide a minimal json object with an error" in {

      val expectedJson =
        Json.parse(
          """
            |{
            | "status": 404,
            | "message": "unable to locate a TrustClaim for the given requests internalId"
            |}
          """.stripMargin
        )

      val errorResponseJson =
        Json.toJson(ErrorResponse(status = NOT_FOUND, message = CLAIM_TRUST_UNABLE_TO_LOCATE, errors = None))

      errorResponseJson mustBe expectedJson
    }

    "be able to provide a json object of with additional storage errors" in {
      val errors = Json.arr(
        Json.obj("index 2" -> Json.arr(Json.obj("code" -> 50, "message" -> "some other mongo write error!"))),
        Json.obj(
          "index 1"        -> Json.arr(
            Json.obj("code" -> 50, "message"  -> "some mongo write error!"),
            Json.obj("code" -> 120, "message" -> "another mongo write error!")
          )
        )
      )

      val expectedJson =
        Json.parse(
          """
            |{
            |  "status": 500,
            |  "message": "unable to store to trusts store",
            |  "errors": [
            |    {
            |      "index 2": [{ "code": 50, "message": "some other mongo write error!" }]
            |    },
            |    {
            |      "index 1": [
            |        { "code": 50, "message": "some mongo write error!" },
            |        { "code": 120, "message": "another mongo write error!" }
            |      ]
            |    }
            |  ]
            |}
          """.stripMargin
        )

      val errorResponseJson =
        Json.toJson(ErrorResponse(status = INTERNAL_SERVER_ERROR, message = UNABLE_TO_STORE, errors = Some(errors)))

      errorResponseJson mustBe expectedJson
    }
  }
}
