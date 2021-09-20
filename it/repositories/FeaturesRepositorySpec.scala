package repositories

import models.flags.FeatureFlag.Enabled
import models.flags.FeatureFlagName.NonTaxableAccessCode
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.Helpers._
import suite.MongoSuite

import scala.concurrent.ExecutionContext.Implicits.global

class FeaturesRepositorySpec
  extends AnyFreeSpec
    with Matchers
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

          val data = Seq(Enabled(NonTaxableAccessCode))

          val result = repo.setFeatureFlags(data).flatMap(_ => repo.getFeatureFlags).futureValue

          result mustBe data
        }
      }
    }
  }
}
