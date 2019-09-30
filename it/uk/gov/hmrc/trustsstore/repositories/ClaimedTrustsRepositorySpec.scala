package uk.gov.hmrc.trustsstore.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import suite.FailOnUnindexedQueries
import uk.gov.hmrc.trustsstore.models.claim_a_trust.TrustClaim

import scala.language.implicitConversions

class ClaimedTrustsRepositorySpec extends FreeSpec with MustMatchers with FailOnUnindexedQueries with IntegrationPatience
  with ScalaFutures with OptionValues with Inside with EitherValues {

  private lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()

  "a claimed trusts repository" - {

    "must be able to store, retrieve and remove trusts claims" in {

      val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

      val application = appBuilder.build()

      running(application) {

        val repository = application.injector.instanceOf[ClaimedTrustsRepository]

        started(application).futureValue

        val trustClaim = TrustClaim(internalId, "1234567890", managedByAgent = true)

        val storedClaim = repository.store(trustClaim).futureValue.right.value

        inside(storedClaim) {
          case TrustClaim(id, utr, mba) =>
            id mustEqual internalId
            utr mustEqual storedClaim.utr
            mba mustEqual storedClaim.managedByAgent
        }

        repository.get(internalId).futureValue.value mustBe trustClaim

        repository.remove(internalId).futureValue

        repository.get(internalId).futureValue mustNot be(defined)
      }
    }
  }
}
