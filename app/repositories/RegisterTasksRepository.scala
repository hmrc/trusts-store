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

import models.maintain.{TaskCache, Tasks}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json, OWrites}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class RegisterTasksRepository @Inject()(override val mongo: ReactiveMongoApi,
                                        override val config: Configuration)
                                       (override implicit val ec: ExecutionContext) extends IndexManager {

  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

  override val collectionName: String = "registerTasks"

  private val expireAfterSeconds = config.get[Int]("mongodb.registerTasks.expireAfterSeconds")

  private val lastUpdatedIndex = MongoIndex(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = "tasks-last-updated-index",
    expireAfterSeconds = Some(expireAfterSeconds)
  )

  private val internalIdAndDraftIdIndex = MongoIndex(
    key = Seq("internalId" -> IndexType.Ascending, "draftId" -> IndexType.Ascending),
    name = "internal-id-and-draft-id-compound-index"
  )

  private def collection: Future[JSONCollection] = for {
    _ <- ensureIndexes
    col <- mongo.database.map(_.collection[JSONCollection](collectionName))
  } yield col

  private def ensureIndexes: Future[Boolean] = for {
    collection                           <- mongo.database.map(_.collection[JSONCollection](collectionName))
    createdLastUpdatedIndex              <- collection.indexesManager.ensure(lastUpdatedIndex)
    internalIdAndIdentifierCompoundIndex <- collection.indexesManager.ensure(internalIdAndDraftIdIndex)
  } yield createdLastUpdatedIndex && internalIdAndIdentifierCompoundIndex

  private def selector(internalId: String, draftId: String): JsObject =
    Json.obj("internalId" -> internalId, "draftId" -> draftId)

  def get(internalId: String, draftId: String): Future[Option[TaskCache]] = {
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
          selector = selector(internalId, draftId),
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

  def set(internalId: String, draftId: String, updated: Tasks): Future[Boolean] = {

    val insertCache = TaskCache(internalId, draftId, updated)

    val modifier = Json.obj(
      "$set" -> Json.toJson(insertCache)
    )

    collection.flatMap {
      _.update(ordered = false).one(selector(internalId, draftId), modifier, upsert = true, multi = false).map {
        result => result.ok
      }
    }
  }

  def reset(internalId: String, draftId: String): Future[Boolean] = {
    set(internalId, draftId, Tasks())
  }
}