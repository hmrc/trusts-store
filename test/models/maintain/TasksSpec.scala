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

package models.maintain

import base.BaseSpec
import models.TaskStatus._
import play.api.libs.json.Json

class TasksSpec extends BaseSpec {

  "Tasks" - {

    "deserialise" - {

      "old style" in {

        val json = Json.parse(
          """
            |{
            |  "trustDetails": false,
            |  "assets": true,
            |  "taxLiability": false,
            |  "trustees": true,
            |  "beneficiaries": false,
            |  "settlors": true,
            |  "protectors": false,
            |  "other": true
            |}
            |""".stripMargin)

        json.as[Tasks] mustBe Tasks(
          trustDetails = InProgress,
          assets = Completed,
          taxLiability = InProgress,
          trustees = Completed,
          beneficiaries = InProgress,
          settlors = Completed,
          protectors = InProgress,
          other = Completed
        )
      }

      "new style" in {

        val json = Json.parse(
          """
            |{
            |  "trustDetails": "in-progress",
            |  "assets": "completed",
            |  "taxLiability": "in-progress",
            |  "trustees": "completed",
            |  "beneficiaries": "in-progress",
            |  "settlors": "completed",
            |  "protectors": "in-progress",
            |  "other": "completed"
            |}
            |""".stripMargin)

        json.as[Tasks] mustBe Tasks(
          trustDetails = InProgress,
          assets = Completed,
          taxLiability = InProgress,
          trustees = Completed,
          beneficiaries = InProgress,
          settlors = Completed,
          protectors = InProgress,
          other = Completed
        )
      }
    }
  }
}
