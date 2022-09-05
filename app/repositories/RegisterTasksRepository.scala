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

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import config.AppConfig
import play.api.Logging
import javax.inject.{Inject, Singleton}
import models.tasks.{TaskCache, Tasks}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, Indexes}
import play.api.libs.json.{Format, JsObject}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class RegisterTasksRepository @Inject()(mongo: MongoComponent,
                                        config: AppConfig)
                                       (implicit ec: ExecutionContext)
  extends PlayMongoRepository[TaskCache] (
    mongoComponent = mongo,
    domainFormat = Format(TaskCache.reads, TaskCache.writes),
    collectionName = "registerTasks",
    indexes = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions().name("tasks-last-updated-index").expireAfter(config.claimAttemptsTtlInSeconds, TimeUnit.SECONDS).unique(false)
      ),
      IndexModel(
        Indexes.ascending("compoundIndex"),
        IndexOptions().name("internal-id-and-identifier-and-sessionId-compound-index")
      )
    )
  ) with Logging {

//  override def collectionName: String = "registerTasks"

   val identifierKey: String = "draftId"

   val useSessionId: Boolean = false

  def get(internalId: String, identifier: String, sessionId: String): Future[Option[TaskCache]] = {
    val modifier = and (equal("$set",LocalDateTime.now),
      equal("lastUpdated",LocalDateTime.now),
      equal("$date", LocalDateTime.now)
    )

    val updateOption = new FindOneAndUpdateOptions().upsert(false)

    collection.findOneAndUpdate(selector(internalId,identifier,sessionId),modifier,updateOption).toFutureOption()

    //    collection
    //      .flatMap(
    //        _.findAndUpdate(
    //          selector = selector(internalId, identifier, sessionId),
    //          update = modifier,
    //          fetchNewObject = true,
    //          upsert = false,
    //          sort = None,
    //          fields = None,
    //          bypassDocumentValidation = false,
    //          writeConcern = WriteConcern.Default,
    //          maxTime = None,
    //          collation = None,
    //          arrayFilters = Nil
    //        ).map(_.result[TaskCache])
    //      )
  }

  private def selector(internalId: String, identifier: String, sessionId: String): Bson = if (useSessionId) {
    equal("newId", s"$internalId-$identifier-$sessionId")
  } else {
    equal("internalIdKey -> internalId", identifierKey -> identifier)
  }

  def set(internalId: String, identifier: String, sessionId: String, updated: Tasks): Future[Boolean] = {

    val insertCache = TaskCache(internalId, identifier, sessionId, updated)

    val modifier = equal("$set",insertCache)

    val updateOption = new FindOneAndUpdateOptions().upsert(true)

    collection.findOneAndUpdate(selector(internalId,identifier,sessionId),modifier,updateOption).toFutureOption().map(_ => true)

    //    collection.flatMap {
    //      _.update(ordered = false).one(selector(internalId, identifier, sessionId), modifier, upsert = true, multi = false).map {
    //        result => result.ok
    //      }
  }

  def reset(internalId: String, identifier: String, sessionId: String): Future[Boolean] = {
    set(internalId, identifier, sessionId, Tasks())
  }

}
