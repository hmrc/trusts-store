/*
 * Copyright 2023 HM Revenue & Customs
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

import com.mongodb.client.model.FindOneAndReplaceOptions
import models.flags.{FeatureFlag, FeatureFlags}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReturnDocument
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class FeaturesRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[FeatureFlags](
      mongoComponent = mongo,
      domainFormat = FeatureFlags.formats,
      collectionName = "features",
      indexes = Seq(),
      replaceIndexes = true
    ) {

  //feature flags don't need ttl
  override lazy val requiresTtlIndex: Boolean = false

  private val featureFlagDocumentId = "feature-flags"
  private val selector              = equal("_id", featureFlagDocumentId)

  def getFeatureFlags: Future[Seq[FeatureFlag]] =
    collection.find(selector).headOption().map(_.map(_.flags).getOrElse(Seq.empty[FeatureFlag]))

  def setFeatureFlags(flags: Seq[FeatureFlag]): Future[Boolean] = {
    val options = new FindOneAndReplaceOptions()
      .upsert(true)
      .returnDocument(ReturnDocument.AFTER)

    collection.findOneAndReplace(selector, FeatureFlags(flags), options).toFutureOption().map(_.isDefined)
  }
}
