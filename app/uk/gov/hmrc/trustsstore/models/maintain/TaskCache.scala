/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.models.maintain

import java.time.LocalDateTime

import play.api.libs.json.{OWrites, Reads, __}
import uk.gov.hmrc.trustsstore.models.MongoDateTimeFormats

case class TaskCache(internalId: String,
                     utr: String,
                     task: Task,
                     lastUpdated: LocalDateTime = LocalDateTime.now)


object TaskCache extends MongoDateTimeFormats {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[TaskCache] = {
    (
      (__ \ "internalId").read[String] and
        (__ \ "utr").read[String] and
        (__ \ "task").read[Task] and
        (__ \ "lastUpdated").read(localDateTimeRead)
      ) (TaskCache.apply _)
  }

  implicit lazy val writes: OWrites[TaskCache] = {
    (
      (__ \ "internalId").write[String] and
        (__ \ "utr").write[String] and
        (__ \ "task").write[Task] and
        (__ \ "lastUpdated").write(localDateTimeWrite)
      ) (unlift(TaskCache.unapply))
  }
}
