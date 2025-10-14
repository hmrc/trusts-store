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

package models.tasks

import play.api.libs.json.{OWrites, Reads, __}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class RegisterTaskCache(
  internalId: String,
  draftId: String,
  id: String,
  sessionId: String,
  task: Tasks,
  lastUpdated: Instant = Instant.now
)

object RegisterTaskCache {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[RegisterTaskCache] =
    (
      (__ \ "internalId").read[String] and
        (__ \ "id").read[String] and
        (__ \ "draftId").read[String] and
        (__ \ "sessionId").readWithDefault[String]("") and
        (__ \ "task").read[Tasks] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(RegisterTaskCache.apply _)

  implicit lazy val writes: OWrites[RegisterTaskCache] =
    (
      (__ \ "internalId").write[String] and
        (__ \ "id").write[String] and
        (__ \ "draftId").write[String] and
        (__ \ "sessionId").write[String] and
        (__ \ "task").write[Tasks] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(RegisterTaskCache.unapply))
}
