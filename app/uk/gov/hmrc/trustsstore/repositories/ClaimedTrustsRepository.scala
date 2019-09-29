/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim
import uk.gov.hmrc.trustsstore.models.claim_a_trust.repository.StorageErrors

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsRepository @Inject()(mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private val collectionName: String = "claimAttempts"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val internalIdIndex = Index(
    key = Seq("internalId" -> IndexType.Ascending),
    name = Some("internal-id-index")
  )

  val started: Future[Unit] =
    collection.flatMap {
      coll =>
        for {
          _ <- coll.indexesManager.ensure(internalIdIndex)
        } yield ()
    }

  def get(internalId: String): Future[Option[TrustClaim]] = {
    collection.flatMap(_.find(Json.obj("internalId" -> internalId)).one[TrustClaim])
  }

  def store(data: TrustClaim): Future[Either[StorageErrors, TrustClaim]] = {
    collection.flatMap(_.insert(ordered = false).one[TrustClaim](data)).map {
      case result if result.writeErrors.nonEmpty => Left(StorageErrors(result.writeErrors))
      case _ => Right(data)
    }
  }

}