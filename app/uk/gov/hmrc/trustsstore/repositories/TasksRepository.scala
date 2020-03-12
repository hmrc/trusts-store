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

import java.sql.Timestamp
import java.time.LocalDateTime

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.trustsstore.models.maintain.{Task, TaskCache}

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class TasksRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration)(implicit ec: ExecutionContext) {

  private val collectionName: String = "maintainTasks"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val expireAfterSeconds = config.get[Int]("mongodb.expireAfterSeconds")

  private val lastUpdatedIndex = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("tasks-last-updated-index"),
    options = BSONDocument("expireAfterSeconds" -> expireAfterSeconds)
  )

  val started: Future[Unit] =
    collection.flatMap {
      coll =>
        for {
          _ <- coll.indexesManager.ensure(lastUpdatedIndex)
        } yield ()
    }

  def get(internalId: String, utr: String): Future[Option[TaskCache]] = {
    val selector = Json.obj("_id" -> internalId, "utr" -> utr)

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "lastUpdated" -> Json.obj(
          "$date" -> Timestamp.valueOf(LocalDateTime.now)
        )
      )
    )

    collection
      .flatMap(
        _.findAndUpdate(selector, modifier, fetchNewObject = true)
        .map(_.result[TaskCache])
      )
  }

  def set(internalId: String, utr: String, updated: Task): Future[Boolean] = {

    val selector = Json.obj("_id" -> internalId, "utr" -> utr)

    val insertCache = TaskCache(internalId, utr, updated)

    val modifier = Json.obj(
      "$set" -> Json.toJson(insertCache)
    )

    collection.flatMap {
      _.update(ordered = false).one(selector, modifier, upsert = true, multi = false).map {
        result => result.ok
      }
    }
  }
}