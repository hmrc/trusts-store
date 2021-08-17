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

import models.flags.FeatureFlag
import play.api.Configuration
import play.api.libs.json.{JsObject, Json, OWrites}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class FeaturesRepository @Inject()(override val mongo: ReactiveMongoApi,
                                   override val config: Configuration)
                                  (implicit val ec: ExecutionContext)
  extends IndexManager {

  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

  override def collectionName: String = "features"

  private val featureFlagDocumentId = "feature-flags"

  private val lastUpdatedIndex = MongoIndex(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = "features-last-updated-index"
  )

  private def collection: Future[JSONCollection] = for {
    _ <- ensureIndexes
    col <- mongo.database.map(_.collection[JSONCollection](collectionName))
  } yield col

  private def ensureIndexes: Future[Boolean] = for {
    collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
    createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
  } yield createdLastUpdatedIndex

  def getFeatureFlags: Future[Seq[FeatureFlag]] =
    collection.flatMap(_.find(Json.obj("_id" -> featureFlagDocumentId), None)
      .one[JsObject])
      .map(_.map(js => (js \ "flags").as[Seq[FeatureFlag]]))
      .map(_.getOrElse(Seq.empty[FeatureFlag]))

  def setFeatureFlags(flags: Seq[FeatureFlag]): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> featureFlagDocumentId
    )

    val modifier = Json.obj(
      "_id" -> featureFlagDocumentId,
      "flags" -> Json.toJson(flags)
    )

    collection.flatMap {
      _.update(ordered = false)
        .one(selector, modifier, upsert = true)
        .map {
          lastError: WriteResult =>
            lastError.ok
        }
    }
  }
}