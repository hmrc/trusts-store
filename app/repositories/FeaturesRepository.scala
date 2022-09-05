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

import config.AppConfig
import models.flags.FeatureFlag
import play.api.libs.json.{Format, JsObject, Json, OWrites}
import javax.inject.{Inject, Singleton}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._

import scala.collection.script.Update
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class FeaturesRepository @Inject()(mongo: MongoComponent,
                                   config: AppConfig)
                                  (implicit ec: ExecutionContext)
  extends PlayMongoRepository[FeatureFlag] (
    mongoComponent = mongo,
    domainFormat = Format(FeatureFlag.reads, FeatureFlag.writes),
    collectionName = "features",
    indexes = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions().name("features-last-updated-index")
      )
    )

  ) {
 //   with IndexManager {

//  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

//  override def collectionName: String = "features"

  private val featureFlagDocumentId = "feature-flags"

//  private val lastUpdatedIndex = MongoIndex(
//    key = Seq("lastUpdated" -> IndexType.Ascending),
//    name = "features-last-updated-index"
//  )

//  private def collection: Future[JSONCollection] = for {
//    _ <- ensureIndexes
//    col <- mongo.database.map(_.collection[JSONCollection](collectionName))
//  } yield col

//  private def ensureIndexes: Future[Boolean] = for {
//    collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
//    createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
//  } yield createdLastUpdatedIndex

//  def getFeatureFlags: Future[Seq[FeatureFlag]] =
//    collection.flatMap(_.find(Json.obj("_id" -> featureFlagDocumentId), None)
//      .one[JsObject])
//      .map(_.map(js => (js \ "flags").as[Seq[FeatureFlag]]))
//      .map(_.getOrElse(Seq.empty[FeatureFlag]))

  def getFeatureFlags: Future[Seq[FeatureFlag]] =
    collection.find[BsonDocument](equal("_id",featureFlagDocumentId)).headOption()
      .map(_.map(bsonDocument =>
      Json.parse(bsonDocument.toJson).as[JsObject])
      .map(json => (json \ "flags").as[Seq[FeatureFlag]])
          .getOrElse(Seq.empty[FeatureFlag])
      )

  def setFeatureFlags(flags: Seq[FeatureFlag]): Future[Boolean] = {

    val selector = equal(
      "_id", featureFlagDocumentId)

    val modifier = equal(
      "flags", Json.toJson(flags))

    val updateOption = new FindOneAndUpdateOptions().upsert(true)

    collection.findOneAndUpdate(selector, modifier, updateOption).toFutureOption().map(_ => true)

//    collection.flatMap {
//      _.update(ordered = false)
//        .one(selector, modifier, upsert = true)
//        .map {
//          lastError: WriteResult =>
//            lastError.ok
//        }
//    }

  }
}