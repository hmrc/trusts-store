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

import java.util.concurrent.TimeUnit

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.claim_a_trust.TrustClaim
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexModel, IndexOptions, _}
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsRepository @Inject()(
                                         mongo: MongoComponent,
                                         config: AppConfig)
                                       (implicit ec: ExecutionContext)
  extends PlayMongoRepository[TrustClaim](
    mongoComponent = mongo,
    domainFormat = Format(TrustClaim.reads, TrustClaim.writes),
    collectionName = "claimAttempts",
    indexes = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions().name("trust-claims-last-updated-index").expireAfter(config.claimAttemptsTtlInSeconds, TimeUnit.SECONDS).unique(false))),
    ){
//    with IndexManager {

//  implicit final val jsObjectWrites: OWrites[JsObject] = OWrites[JsObject](identity)

//  override def collectionName: String = "claimAttempts"

//  private val expireAfterSeconds = config.get[Int]("mongodb.claimAttempts.expireAfterSeconds")

//  private val lastUpdatedIndex = MongoIndex(
//    key = Seq("lastUpdated" -> IndexType.Ascending),
//    name = "trust-claims-last-updated-index",
//    expireAfterSeconds = Some(expireAfterSeconds)
//  )

//  private def collection: Future[JSONCollection] = for {
//   _ <- ensureIndexes
//   col <- mongo.database.map(_.collection[JSONCollection](collectionName))
//  } yield col

//  private def ensureIndexes: Future[Boolean] = for {
//    collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
//    createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
//  } yield createdLastUpdatedIndex

  def get(internalId: String): Future[Option[TrustClaim]] =
//    collection.flatMap(_.find(Json.obj("_id" -> internalId), projection = None).one[TrustClaim])
    collection.find(equal("_id", internalId)).headOption()

//  def remove(internalId: String): Future[Option[TrustClaim]] =
//    collection.flatMap(_.findAndRemove(
//      Json.obj("_id" -> internalId),
//      None,
//      None,
//      WriteConcern.Default,
//      None,
//      None,
//      Seq.empty
//    ).map(_.result[TrustClaim]))

  def remove(internalId: String): Future[Option[TrustClaim]] =
    collection.findOneAndDelete(equal("id", internalId)).toFutureOption()


  def store(trustClaim: TrustClaim): Future[TrustClaim] = {

    val selector = equal(
      "_id", trustClaim.internalId
    )

    val modifier = equal(
      "$set", trustClaim
    )

    val updateOption = new FindOneAndUpdateOptions().upsert(true)

    collection.findOneAndUpdate(selector, modifier, updateOption).toFuture()

//    collection.flatMap(_.update.one(q = selector, u = modifier, upsert = true, multi = false)).map {
//      case result if result.writeErrors.nonEmpty => Left(StorageErrors(result.writeErrors))
//      case _ => Right(trustClaim)
//    }
  }
}