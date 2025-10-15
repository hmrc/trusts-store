/*
 * Copyright 2025 HM Revenue & Customs
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

package models.claim_a_trust

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class TrustClaim(
  internalId: String,
  identifier: String,
  managedByAgent: Boolean,
  trustLocked: Boolean = false,
  lastUpdated: Instant = Instant.now
) {

  def toResponse: JsObject =
    Json.obj(
      "internalId"     -> this.internalId,
      "id"             -> this.identifier,
      "managedByAgent" -> this.managedByAgent,
      "trustLocked"    -> this.trustLocked
    )
}

object TrustClaim {
  implicit lazy val reads: Reads[TrustClaim] =
    (
      (__ \ "_id").read[String] and
        (__ \ "id").read[String] and
        (__ \ "managedByAgent").read[Boolean] and
        (__ \ "trustLocked").read[Boolean] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(TrustClaim.apply _)

  implicit lazy val writes: OWrites[TrustClaim] =
    (
      (__ \ "_id").write[String] and
        (__ \ "id").write[String] and
        (__ \ "managedByAgent").write[Boolean] and
        (__ \ "trustLocked").write[Boolean] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(TrustClaim.unapply))
}
