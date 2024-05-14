/*
 * Copyright 2024 HM Revenue & Customs
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
import models.tasks.{Task, TaskStatus, Tasks}
import org.mockito.ArgumentMatchers.{eq => mEq, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.libs.json.Json
import repositories.RegisterTasksRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegisterTasksServiceSpec extends BaseSpec {

  private val repository = mock[RegisterTasksRepository]

  private val service = new RegisterTasksService(repository)

  override def beforeEach(): Unit =
    Mockito.reset(repository)

  "invoking .get" should {

    "return a Task from the repository if there is one for the given internal id and draft id" in {

      val task = Tasks()

      when(repository.get(mEq("internalId"), mEq("draftId"))).thenReturn(Future.successful(Some(task)))

      val result = service.get("internalId", "draftId").futureValue

      result mustBe task
    }

    "return a Task from the repository if there is not one for the given internal id and draft id" in {
      val task = Tasks()

      when(repository.get(mEq("internalId"), mEq("draftId"))).thenReturn(Future.successful(None))

      val result = service.get("internalId", "draftId").futureValue

      result mustBe task
    }
  }

  "invoking .set" should {

    "set an updated Task" in {

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

      when(repository.set(any(), any(), any(), any())).thenReturn(Future.successful(Some(task)))

      val result = service.set(fakeInternalId, fakeUtr, fakeSessionId, task).futureValue

      result mustBe task
    }
  }

  "invoking .reset" should {

    "reset task list" in {

      when(repository.reset(any(), any(), any())).thenReturn(Future.successful(Some(Tasks())))

      val result = service.reset(fakeInternalId, fakeUtr, fakeSessionId).futureValue

      result mustBe true
    }
  }

  "invoking .modifyTask" should {
    val tasks        = Seq(
      Task.TrustDetails,
      Task.Assets,
      Task.TaxLiability,
      Task.Trustees,
      Task.Beneficiaries,
      Task.Protectors,
      Task.Settlors,
      Task.OtherIndividuals
    )
    val taskStatuses = TaskStatus.values.toSeq

    tasks.foreach { task =>
      taskStatuses.foreach { taskStatus =>
        s"modify task of type=$task to status=$taskStatus" in {
          val inputTasks    = Tasks()
          val expectedTasks = task match {
            case Task.TrustDetails     => inputTasks.copy(trustDetails = taskStatus)
            case Task.Assets           => inputTasks.copy(assets = taskStatus)
            case Task.TaxLiability     => inputTasks.copy(taxLiability = taskStatus)
            case Task.Trustees         => inputTasks.copy(trustees = taskStatus)
            case Task.Beneficiaries    => inputTasks.copy(beneficiaries = taskStatus)
            case Task.Protectors       => inputTasks.copy(protectors = taskStatus)
            case Task.Settlors         => inputTasks.copy(settlors = taskStatus)
            case Task.OtherIndividuals => inputTasks.copy(other = taskStatus)
          }

          reset(repository)
          when(repository.get(any(), any())).thenReturn(Future.successful(Some(Tasks())))
          when(repository.set(any(), any(), any(), any())).thenReturn(Future.successful(Some(Tasks())))

          val result = service.modifyTask(fakeInternalId, fakeUtr, fakeSessionId, task, taskStatus).futureValue

          result mustBe Json.toJson(expectedTasks)
        }
      }
    }
  }
}
