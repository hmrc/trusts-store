package repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import models.FeatureFlag.Enabled
import models.FeatureFlagName.`5MLD`
import org.scalatest.matchers.must.Matchers
import suite.MongoSuite
import org.scalatest.freespec.AnyFreeSpec


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

          val data = Seq(Enabled(`5MLD`))

          whenReady(repo.setFeatureFlags(data).flatMap(_ => repo.getFeatureFlags)) { result =>
            result mustBe data
          }
        }
      }
    }
  }
}
