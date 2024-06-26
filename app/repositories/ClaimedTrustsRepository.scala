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
import models.claim_a_trust.TrustClaim
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsRepository @Inject() (mongo: MongoComponent, config: AppConfig)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[TrustClaim](
      mongoComponent = mongo,
      domainFormat = Format(TrustClaim.reads, TrustClaim.writes),
      collectionName = "claimAttempts",
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("trust-claims-last-updated-index")
            .expireAfter(config.claimAttemptsTtlInSeconds, TimeUnit.SECONDS)
            .unique(false)
        )
      ),
      replaceIndexes = config.dropIndexes
    ) {

  def get(internalId: String): Future[Option[TrustClaim]] =
    collection.find(equal("_id", internalId)).headOption()

  def store(trustClaim: TrustClaim): Future[TrustClaim] = {
    val selector     = equal("_id", trustClaim.internalId)
    val updateOption = new FindOneAndReplaceOptions()
      .upsert(true)
      .returnDocument(ReturnDocument.AFTER)

    collection.findOneAndReplace(selector, trustClaim, updateOption).toFuture()
  }
}
