package uk.gov.hmrc.trustsstore.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.test.Helpers._
import uk.gov.hmrc.trustsstore.models.FeatureFlag.Enabled
import uk.gov.hmrc.trustsstore.models.FeatureFlagName.MLD5
import uk.gov.hmrc.trustsstore.suite.MongoSuite

import scala.concurrent.ExecutionContext.Implicits.global

class FeaturesRepositorySpec
  extends FreeSpec
    with MustMatchers
    with ScalaFutures
    with OptionValues
    with MongoSuite
    with IntegrationPatience {

  "Features Repository" - {

    "must round trip feature flags correctly" in {

      running(application) {
        getConnection(application).map { connection =>

          val repo = application.injector.instanceOf[FeaturesRepository]

          dropTheDatabase(connection)

          val data = Seq(Enabled(MLD5))

          whenReady(repo.setFeatureFlags(data).flatMap(_ => repo.getFeatureFlags)) { result =>
            result mustBe data
          }
        }
      }
    }
  }
}
