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

package uk.gov.hmrc.trustsstore.services

import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.TrustClaim
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository

import scala.concurrent.Future

class ClaimedTrustsServiceSpec extends BaseSpec {

  private val repository = mock[ClaimedTrustsRepository]

  lazy val application: Application = applicationBuilder().overrides(
    bind[ClaimedTrustsRepository].toInstance(repository)
  ).build()

  private val service = application.injector.instanceOf[ClaimedTrustsService]

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
  }

  "invoking .get" - {
    "should return a TrustClaim from the repository if there is one for the given internal id" in {
      val trustClaim = TrustClaim(utr = fakeUtr, managedByAgent = true)

      when(repository.get(any())).thenReturn(Future.successful(Some(trustClaim)))

      val result = service.get("matching-internal-id").futureValue

      result mustBe Some(trustClaim)
    }

    "should return None from the repository if there is no claims for the given internal id" in {
      when(repository.get(any())).thenReturn(Future.successful(None))

      val result = service.get("unmatched-internal-id").futureValue

      result mustBe None
    }
  }

  "invoking POST /claim" - {
    "should store"
  }

}
