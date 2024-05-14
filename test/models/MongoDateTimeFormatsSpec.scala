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

package models

import base.BaseSpec
import play.api.libs.json.{JsString, Json}

import java.time.{LocalDate, LocalDateTime}

class MongoDateTimeFormatsSpec extends BaseSpec with MongoDateTimeFormats {

  "a LocalDateTime" should {

    val date = LocalDate.of(2018, 2, 1).atStartOfDay

    val dateMillis = 1517443200000L

    val jsonMiliseconds         = Json.obj(
      s"$$date" -> dateMillis
    )
    val jsonWrappedLong         = Json.obj(
      s"$$date" -> Json.obj(s"$$numberLong" -> dateMillis.toString)
    )
    val jsonStringZonedDateTime = Json.obj(
      s"$$date" -> JsString("2018-02-01T00:00:00.000Z")
    )
    val jsonStringDateTime      = Json.obj(
      s"$$date" -> JsString("2018-02-01T00:00:00.000000")
    )

    "serialise to jsonMiliseconds" in {
      val result = Json.toJson(date)
      result mustEqual jsonMiliseconds
    }

    "deserialise from jsonMiliseconds" in {
      val result = jsonMiliseconds.as[LocalDateTime]
      result mustEqual date
    }

    "serialise/deserialise to the same value jsonMiliseconds" in {
      val result = Json.toJson(date).as[LocalDateTime]
      result mustEqual date
    }

    "deserialise from jsonWrappedLong" in {
      val result = jsonWrappedLong.as[LocalDateTime]
      result mustEqual date
    }

    "deserialise from jsonStringZonedDateTime" in {
      val result = jsonStringZonedDateTime.as[LocalDateTime]
      result mustEqual date
    }

    "deserialise from jsonStringDateTime" in {
      val result = jsonStringDateTime.as[LocalDateTime]
      result mustEqual date
    }
  }
}
