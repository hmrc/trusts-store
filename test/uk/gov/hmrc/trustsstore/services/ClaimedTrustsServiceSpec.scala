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

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, FreeSpec, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.TrustClaim

import scala.concurrent.Future

class ClaimedTrustsServiceSpec extends BaseSpec {

  private val service = app.injector.instanceOf[ClaimedTrustsService]

  private val repository = mock[ClaimedTrustsRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
  }

  "invoking .get" - {
    "should return a TrustClaim" in {

      val trustClaim = TrustClaim(utr = fakeUtr, managedByAgent = true)

      when(repository.get(any())).thenReturn(Future.successful(Some(trustClaim)))

      val result = service.get()

      result mustBe trustClaim
    }
  }

  "invoking POST /claim" - {
    "should store"
  }

}
