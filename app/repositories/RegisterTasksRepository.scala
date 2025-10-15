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

package repositories

import config.AppConfig
import models.UpdatedCounterValues
import models.tasks.{RegisterTaskCache, Tasks}
import org.bson.BsonType
import org.bson.types.ObjectId
import org.mongodb.scala.Observable
import org.mongodb.scala.bson.{BsonDateTime, BsonDocument}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model._
import play.api.Logging
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Instant, LocalDateTime}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton()
class RegisterTasksRepository @Inject() (mongo: MongoComponent, config: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[RegisterTaskCache](
      mongoComponent = mongo,
      domainFormat = Format(RegisterTaskCache.reads, RegisterTaskCache.writes),
      collectionName = "registerTasks",
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("register-tasks-last-updated-index")
            .expireAfter(config.registerTaskTtlInSeconds, TimeUnit.SECONDS)
            .unique(false)
        ),
        IndexModel(
          Indexes.ascending("internalId", "draftId"),
          IndexOptions().name("internal-id-and-identifier-compound-index")
        )
      ),
      replaceIndexes = config.dropIndexes
    )
    with Logging {
  val className = this.getClass.getSimpleName

  private val internalIdKey: String = "internalId"
  private val identifierKey: String = "draftId"

  private def selector(internalId: String, identifier: String): Bson =
    and(
      equal(internalIdKey, internalId),
      equal(identifierKey, identifier)
    )

  def get(internalId: String, identifier: String): Future[Option[Tasks]] = {
    val modifier = Updates.set("lastUpdated", LocalDateTime.now)

    val updateOption = new FindOneAndUpdateOptions()
      .upsert(false)
      .returnDocument(ReturnDocument.AFTER)

    collection
      .findOneAndUpdate(selector(internalId, identifier), modifier, updateOption)
      .toFutureOption()
      .map(_.map(_.task))
  }

  def set(internalId: String, identifier: String, sessionId: String, updated: Tasks): Future[Option[Tasks]] = {
    val draftIdValue = identifier
    val insertCache  = RegisterTaskCache(internalId, identifier, draftIdValue, sessionId, updated)

    val updateOption = new FindOneAndReplaceOptions()
      .upsert(true)
      .returnDocument(ReturnDocument.AFTER)

    collection
      .findOneAndReplace(selector(internalId, identifier), insertCache, updateOption)
      .toFutureOption()
      .map(_.map(_.task))
  }

  def reset(internalId: String, identifier: String, sessionId: String): Future[Option[Tasks]] =
    set(internalId, identifier, sessionId, Tasks())

  def getAllInvalidDateDocuments(limit: Int): Observable[ObjectId] = {
    logger.info(s"[$className][getAllInvalidDateDocuments] started fetching invalid documents")
    val selector = Filters.not(Filters.`type`("lastUpdated", BsonType.DATE_TIME))
    val sortById = Sorts.ascending("_id")
    collection
      .find[BsonDocument](selector)
      .sort(sortById)
      .limit(limit)
      .map(jsToObjectId)
  }

  private def jsToObjectId(js: BsonDocument): ObjectId =
    Try(js.getObjectId("_id").getValue) match {
      case Failure(exception) =>
        logger.error(s"[$className][jsToObjectId] failed to fetch id from : $collectionName", exception)
        throw new Exception("_id is not found")
      case Success(value)     => value
    }

  def updateAllInvalidDateDocuments(ids: Seq[ObjectId]): Future[UpdatedCounterValues] = {
    val update   = Updates.set("lastUpdated", BsonDateTime(Instant.now().toEpochMilli))
    val filterIn = Filters.in("_id", ids: _*)
    collection
      .updateMany(filterIn, update)
      .toFuture()
      .map(_ => UpdatedCounterValues(matched = ids.size, updated = ids.size, errors = 0))
      .recover { case _ => UpdatedCounterValues(matched = ids.size, updated = 0, errors = ids.size) }
  }
}
