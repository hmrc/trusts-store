/*
 * Copyright 2022 HM Revenue & Customs
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
import models.tasks.Task.TrustDetails
import models.tasks.TaskStatus._
import models.tasks.{TaskCache, Tasks}
import org.mockito.ArgumentMatchers.{eq => mEq, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import repositories.RegisterTasksRepository

import java.time.LocalDateTime
import scala.concurrent.Future

class RegisterTasksServiceSpec extends BaseSpec {

  private val repository = mock[RegisterTasksRepository]

  lazy val application: Application = applicationBuilder().overrides(
    bind[RegisterTasksRepository].toInstance(repository)
  ).build()

  private val service = application.injector.instanceOf[RegisterTasksService]

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
  }

  "invoking .get" - {

    "must return a Task from the repository if there is one for the given internal id and draft id" in {

      val task = Tasks()

      val taskCache = TaskCache(
        "internalId",
        "draftId",
        task,
        LocalDateTime.now
      )

      when(repository.get(mEq("internalId"), mEq("draftId"))).thenReturn(Future.successful(Some(taskCache)))

      val result = service.get("internalId", "draftId").futureValue

      result mustBe task
    }

    "must return a Task from the repository if there is not one for the given internal id and draft id" in {
      val task = Tasks()

      when(repository.get(mEq("internalId"), mEq("draftId"))).thenReturn(Future.successful(None))

      val result = service.get("internalId", "draftId").futureValue

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

  "invoking .modifyTask" - {

    "must modify a given Task status" in {

      val startingTasks = Tasks(
        trustDetails = InProgress,
        assets = InProgress,
        taxLiability = InProgress,
        trustees = InProgress,
        settlors = InProgress,
        protectors = InProgress,
        beneficiaries = InProgress,
        other = InProgress
      )

      val updatedTasks = Tasks(
        trustDetails = Completed,
        assets = InProgress,
        taxLiability = InProgress,
        trustees = InProgress,
        settlors = InProgress,
        protectors = InProgress,
        beneficiaries = InProgress,
        other = InProgress
      )

      val taskCache = TaskCache(
        fakeInternalId,
        fakeUtr,
        startingTasks,
        LocalDateTime.now
      )

      when(repository.get(mEq(fakeInternalId), mEq(fakeUtr))).thenReturn(Future.successful(Some(taskCache)))

      when(repository.set(any(), any(), any())).thenReturn(Future.successful(true))

      val result = service.modifyTask(fakeInternalId, fakeUtr, TrustDetails, Completed).futureValue

      result mustBe Json.toJson(updatedTasks)
    }
  }
}
