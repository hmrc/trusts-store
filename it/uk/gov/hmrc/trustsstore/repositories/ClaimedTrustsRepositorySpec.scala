package uk.gov.hmrc.trustsstore.repositories

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import suite.FailOnUnindexedQueries
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class ClaimedTrustsRepositorySpec extends FreeSpec with MustMatchers with FailOnUnindexedQueries with IntegrationPatience
  with ScalaFutures with OptionValues with Inside with EitherValues with Eventually {

  private lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()

  "a claimed trusts repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

    "must be able to store, retrieve and remove trusts claims" in {

      database.map(_.drop()).futureValue

      val application = appBuilder.build()

      running(application) {

        val repository = application.injector.instanceOf[ClaimedTrustsRepository]

        started(application).futureValue

        val lastUpdated = LocalDateTime.parse("2000-01-01 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true, lastUpdated = lastUpdated)

        val storedClaim = repository.store(trustClaim).futureValue.right.value

        inside(storedClaim) {
          case TrustClaim(id, utr, mba, ldt) =>
            id mustEqual internalId
            utr mustEqual storedClaim.utr
            mba mustEqual storedClaim.managedByAgent
            ldt mustEqual storedClaim.lastUpdated
        }

        repository.get(internalId).futureValue.value mustBe trustClaim

        repository.remove(internalId).futureValue

        repository.get(internalId).futureValue mustNot be(defined)
      }
    }

    "must ensure indexes with the ttl" in {


      database.map(_.drop()).futureValue

      val ttl = 123
      val application = appBuilder.configure(Map("mongodb.expireAfterSeconds" -> ttl)).build()

      running(application) {

        val repository = application.injector.instanceOf[ClaimedTrustsRepository]

        started(application).futureValue

        val indices = database.flatMap {
          _.collection[JSONCollection]("claimAttempts")
            .indexesManager.list()
        }.futureValue

        indices.find {
          index =>
            index.name.contains("trust-claims-last-updated-index") &&
            index.key == Seq("lastUpdated" -> IndexType.Ascending) &&
            index.options == BSONDocument("expireAfterSeconds" -> ttl)
        } mustBe defined
      }
    }
  }
}
