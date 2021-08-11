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

package repositories

import javax.inject.{Inject, Singleton}
import models.claim_a_trust.TrustClaim
import models.repository.StorageErrors
import play.api.Configuration
import play.api.libs.json.{JsObject, Json, OWrites}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsRepository @Inject()(override val mongo: ReactiveMongoApi,
                                        override val config: Configuration)
                                       (override implicit val ec: ExecutionContext)
  extends IndexManager {

  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

  override def collectionName: String = "claimAttempts"

  private val expireAfterSeconds = config.get[Int]("mongodb.claimAttempts.expireAfterSeconds")

  private val lastUpdatedIndex = MongoIndex(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = "trust-claims-last-updated-index",
    expireAfterSeconds = Some(expireAfterSeconds)
  )

  private def collection: Future[JSONCollection] = for {
   _ <- ensureIndexes
   col <- mongo.database.map(_.collection[JSONCollection](collectionName))
  } yield col

  private def ensureIndexes: Future[Boolean] = for {
    collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
    createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
  } yield createdLastUpdatedIndex

  def get(internalId: String): Future[Option[TrustClaim]] =
    collection.flatMap(_.find(Json.obj("_id" -> internalId), projection = None).one[TrustClaim])

  def remove(internalId: String): Future[Option[TrustClaim]] =
    collection.flatMap(_.findAndRemove(
      Json.obj("_id" -> internalId),
      None,
      None,
      WriteConcern.Default,
      None,
      None,
      Seq.empty
    ).map(_.result[TrustClaim]))

  def store(trustClaim: TrustClaim): Future[Either[StorageErrors, TrustClaim]] = {

    val selector = Json.obj(
      "_id" -> trustClaim.internalId
    )

    val modifier = Json.obj(
      "$set" -> trustClaim
    )

    collection.flatMap(_.update.one(q = selector, u = modifier, upsert = true, multi = false)).map {
      case result if result.writeErrors.nonEmpty => Left(StorageErrors(result.writeErrors))
      case _ => Right(trustClaim)
    }
  }
}