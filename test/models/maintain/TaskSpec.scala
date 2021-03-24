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
import play.api.libs.json.Json

class TaskSpec extends BaseSpec {

  "Task" - {

    "must read from json" - {

      "when 4mld" in {

        val json = Json.parse(
          """
            |{
            |  "trustees": false,
            |  "beneficiaries": false,
            |  "settlors": false,
            |  "protectors": false,
            |  "other": false
            |}
            |""".stripMargin)

        json.as[Task] mustBe Task(
          trustDetails = true,
          trustees = false,
          beneficiaries = false,
          settlors = false,
          protectors = false,
          other = false,
          nonEeaCompany = true
        )
      }

      "when 5mld" in {

        val json = Json.parse(
          """
            |{
            |  "trustDetails": false,
            |  "trustees": false,
            |  "beneficiaries": false,
            |  "settlors": false,
            |  "protectors": false,
            |  "other": false,
            |  "nonEeaCompany": false
            |}
            |""".stripMargin)

        json.as[Task] mustBe Task(
          trustDetails = false,
          trustees = false,
          beneficiaries = false,
          settlors = false,
          protectors = false,
          other = false,
          nonEeaCompany = false
        )
      }
    }
  }
}
