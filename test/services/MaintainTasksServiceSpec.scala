/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import base.BaseSpec
import models.tasks.TaskStatus._
import models.tasks.{TaskCache, Tasks}
import org.mockito.Matchers.{eq => mEq, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import repositories.MaintainTasksRepository

import java.time.LocalDateTime
import scala.concurrent.Future

class MaintainTasksServiceSpec extends BaseSpec {

  private val repository = mock[MaintainTasksRepository]

  lazy val application: Application = applicationBuilder().overrides(
    bind[MaintainTasksRepository].toInstance(repository)
  ).build()

  private val service = application.injector.instanceOf[MaintainTasksService]

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
  }

  "invoking .get" - {

    "must return a Task from the repository if there is one for the given internal id and utr" in {

      val task = Tasks()

      val taskCache = TaskCache(
        "internalId",
        "utr",
        task,
        LocalDateTime.now
      )

      when(repository.get(mEq("internalId"), mEq("utr"))).thenReturn(Future.successful(Some(taskCache)))

      val result = service.get("internalId", "utr").futureValue

      result mustBe task
    }

    "must return a Task from the repository if there is not one for the given internal id and utr" in {
      val task = Tasks()

      when(repository.get(mEq("internalId"), mEq("utr"))).thenReturn(Future.successful(None))

      val result = service.get("internalId", "utr").futureValue

      result mustBe task
    }
  }

  "invoking .set" - {

    "must set an updated Task" in {

      val task = Tasks(
        trustDetails = InProgress,
        assets = InProgress,
        taxLiability = InProgress,
        trustees = Completed,
        settlors = Completed,
        protectors = InProgress,
        beneficiaries = InProgress,
        other = InProgress
      )

      when(repository.set(any(), any(), any())).thenReturn(Future.successful(true))

      val result = service.set(fakeInternalId, fakeUtr, task).futureValue

      result mustBe task
    }
  }

  "invoking .reset" - {

    "must reset task list" in {

      when(repository.reset(any(), any())).thenReturn(Future.successful(true))

      val result = service.reset(fakeInternalId, fakeUtr).futureValue

      result mustBe true
    }
  }

}
