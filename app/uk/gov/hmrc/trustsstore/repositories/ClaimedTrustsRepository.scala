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

package uk.gov.hmrc.trustsstore.repositories

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim
import uk.gov.hmrc.trustsstore.models.repository.StorageErrors

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration)(implicit ec: ExecutionContext) {

  private val collectionName: String = "claimAttempts"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val expireAfterSeconds = config.get[Int]("mongodb.expireAfterSeconds")

  private val lastUpdatedIndex = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("trust-claims-last-updated-index"),
    options = BSONDocument("expireAfterSeconds" -> expireAfterSeconds)
  )

  val started: Future[Unit] =
    collection.flatMap {
      coll =>
        for {
          _ <- coll.indexesManager.ensure(lastUpdatedIndex)
        } yield ()
    }

  def get(internalId: String): Future[Option[TrustClaim]] =
    collection.flatMap(_.find(Json.obj("_id" -> internalId), projection = None).one[TrustClaim])

  def remove(internalId: String): Future[Option[TrustClaim]] =
    collection.flatMap(_.findAndRemove(Json.obj("_id" -> internalId)).map(_.result[TrustClaim]))

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