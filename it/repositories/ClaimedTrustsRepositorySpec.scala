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

import models.claim_a_trust.TrustClaim
import org.mongodb.scala.model.Filters
import play.api.Logging
import uk.gov.hmrc.mongo.test.PlayMongoRepositorySupport

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class ClaimedTrustsRepositorySpec
    extends RepositoriesBaseSpec
    with PlayMongoRepositorySupport[TrustClaim]
    with Logging {

  val internalId                               = "Int-328969d0-557e-4559-96ba-074d0597107e"
  lazy val repository: ClaimedTrustsRepository = new ClaimedTrustsRepository(mongoComponent, appConfig)

  private def remove(internalId: String): Boolean = {
    logger.info(s"Delete Claim with internalId=$internalId")
    val res = repository.collection.deleteMany(Filters.equal("_id", internalId)).toFutureOption().futureValue
    logger.info(s"Deleted ${res.get.getDeletedCount}")
    res.exists(_.wasAcknowledged()) && res.get.getDeletedCount >= 1
  }

  "a claimed trusts repository" should {

    "be able to store, retrieve and remove trusts claims" in {

      cleanData(repository.collection)

      val lastUpdated = LocalDateTime.parse("2000-01-01 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

      val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true, lastUpdated = lastUpdated)

      val storedClaim = repository.store(trustClaim).futureValue

      storedClaim mustBe trustClaim

      repository.get(internalId).futureValue.value mustBe trustClaim

      remove(internalId) mustBe true

      repository.get(internalId).futureValue mustBe None
    }

    "be able to update a trust claim with the same auth id" in {

      cleanData(repository.collection)

      val lastUpdated = LocalDateTime.parse("2000-01-01 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

      val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true, lastUpdated = lastUpdated)

      repository.store(trustClaim).futureValue

      val toUpdate     = trustClaim.copy(identifier = "0987654321")
      val updatedClaim = repository.store(toUpdate).futureValue

      toUpdate mustBe updatedClaim
      repository.get(internalId).futureValue.value mustBe toUpdate
      repository.get(internalId).futureValue.value mustBe updatedClaim
    }
  }
}
