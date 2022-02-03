/*
 * Copyright 2022 HM Revenue & Customs
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

import models.tasks.{TaskCache, Tasks}
import play.api.libs.json.{JsObject, Json, OWrites}
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection
import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.Future

trait TasksRepository extends IndexManager {

  implicit final lazy val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

  lazy val expireAfterSeconds: Int = config.get[Int](s"mongodb.$collectionName.expireAfterSeconds")

  private val internalIdKey: String = "internalId"
  val identifierKey: String
  private val newId: String = "newId"
  val useSessionId: Boolean

  private lazy val lastUpdatedIndex = MongoIndex(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = "tasks-last-updated-index",
    expireAfterSeconds = Some(expireAfterSeconds)
  )

  private lazy val compoundIndex = if (useSessionId) {
    MongoIndex(
      key = Seq(internalIdKey -> IndexType.Ascending, identifierKey -> IndexType.Ascending, newId -> IndexType.Ascending),
      name = "internal-id-and-identifier-and-newId-compound-index"
    )
  } else {
    MongoIndex(
      key = Seq(internalIdKey -> IndexType.Ascending, identifierKey -> IndexType.Ascending),
      name = "internal-id-and-identifier-compound-index"
    )
  }

  private def collection: Future[JSONCollection] = for {
    _ <- ensureIndexes
    col <- mongo.database.map(_.collection[JSONCollection](collectionName))
  } yield col

  private def ensureIndexes: Future[Boolean] = for {
    collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
    createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
    internalIdAndIdentifierCompoundIndex <- collection.indexesManager.ensure(compoundIndex)
  } yield createdLastUpdatedIndex && internalIdAndIdentifierCompoundIndex

  private def selector(internalId: String, identifier: String, sessionId: String): JsObject = if (useSessionId) {
    Json.obj(internalIdKey -> internalId, identifierKey -> identifier, newId -> s"$internalId-$identifier-$sessionId")
  } else {
    Json.obj(internalIdKey -> internalId, identifierKey -> identifier)
  }


  def get(internalId: String, identifier: String, sessionId: String): Future[Option[TaskCache]] = {
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "lastUpdated" -> Json.obj(
          "$date" -> Timestamp.valueOf(LocalDateTime.now)
        )
      )
    )

    collection
      .flatMap(
        _.findAndUpdate(
          selector = selector(internalId, identifier, sessionId),
          update = modifier,
          fetchNewObject = true,
          upsert = false,
          sort = None,
          fields = None,
          bypassDocumentValidation = false,
          writeConcern = WriteConcern.Default,
          maxTime = None,
          collation = None,
          arrayFilters = Nil
        ).map(_.result[TaskCache])
      )
  }

  def set(internalId: String, identifier: String, sessionId: String, updated: Tasks): Future[Boolean] = {

    val insertCache = TaskCache(internalId, identifier, sessionId, updated)

    val modifier = Json.obj(
      "$set" -> Json.toJson(insertCache)
    )

    collection.flatMap {
      _.update(ordered = false).one(selector(internalId, identifier, sessionId), modifier, upsert = true, multi = false).map {
        result => result.ok
      }
    }
  }

  def reset(internalId: String, identifier: String, sessionId: String): Future[Boolean] = {
    set(internalId, identifier, sessionId, Tasks())
  }

}
