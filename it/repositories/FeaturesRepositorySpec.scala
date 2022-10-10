package repositories

import models.flags.FeatureFlag.{Disabled, Enabled}
import models.flags.FeatureFlagName.{NonTaxableAccessCode, `5MLD`}
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global

class FeaturesRepositorySpec extends RepositoriesBaseSpec with MongoSupport {

  lazy val repository: FeaturesRepository = new FeaturesRepository(mongoComponent, appConfig)

  "Features Repository" should {

    "remove all flag correctly" in {
      val data = Seq()

      val setFlags = repository.setFeatureFlags(data).futureValue
      setFlags mustBe true

      val result = repository.getFeatureFlags.futureValue
      result mustBe data
    }

    "save one flag correctly" in {
      val data = Seq(Enabled(`5MLD`))

      val setFlags = repository.setFeatureFlags(data).futureValue
      setFlags mustBe true

      val result = repository.getFeatureFlags.futureValue
      result mustBe data
    }

    "save two flags correctly" in {
      val data = Seq(Enabled(`5MLD`), Disabled(NonTaxableAccessCode))

      val setFlags = repository.setFeatureFlags(data).futureValue
      setFlags mustBe true

      val result = repository.getFeatureFlags.futureValue
      result mustBe data
    }
  }
}
