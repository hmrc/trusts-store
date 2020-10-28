package repositories

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import models.claim_a_trust.TrustClaim
import suite.MongoSuite

import scala.language.implicitConversions

class ClaimedTrustsRepositorySpec extends FreeSpec with MustMatchers
  with ScalaFutures with OptionValues with Inside with MongoSuite with EitherValues {

  "a claimed trusts repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

    "must be able to store, retrieve and remove trusts claims" in {

      running(application) {

        getConnection(application).map { connection =>

          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[ClaimedTrustsRepository]

          val lastUpdated = LocalDateTime.parse("2000-01-01 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

          val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true, lastUpdated = lastUpdated)

          val storedClaim = repository.store(trustClaim).futureValue.right.value

          inside(storedClaim) {
            case TrustClaim(id, utr, mba, tl, ldt) =>
              id mustEqual internalId
              utr mustEqual storedClaim.utr
              mba mustEqual storedClaim.managedByAgent
              tl mustEqual storedClaim.trustLocked
              ldt mustEqual storedClaim.lastUpdated
          }

          repository.get(internalId).futureValue.value mustBe trustClaim

          repository.remove(internalId).futureValue

          repository.get(internalId).futureValue mustNot be(defined)

          dropTheDatabase(connection)
        }

      }
    }

    "must be able to update a trust claim with the same auth id" in {

      running(application) {

        getConnection(application).map { connection =>

          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[ClaimedTrustsRepository]

          val lastUpdated = LocalDateTime.parse("2000-01-01 12:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

          val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true, lastUpdated = lastUpdated)

          repository.store(trustClaim).futureValue.right.value

          val updatedClaim = repository.store(trustClaim.copy(utr = "0987654321")).futureValue

          updatedClaim must be ('right)

          dropTheDatabase(connection)
        }

      }
    }

  }
}
