/*
 * Copyright 2025 HM Revenue & Customs
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

import models.flags.FeatureFlag.{Disabled, Enabled}
import models.flags.FeatureFlagName.{NonTaxableAccessCode, `5MLD`}
import models.flags.FeatureFlags
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class FeaturesRepositorySpec extends RepositoriesBaseSpec with DefaultPlayMongoRepositorySupport[FeatureFlags] {

  lazy val repository: FeaturesRepository = new FeaturesRepository(mongoComponent)

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
