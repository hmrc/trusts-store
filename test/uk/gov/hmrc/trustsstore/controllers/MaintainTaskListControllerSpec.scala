/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.controllers

import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.models.maintain.Task
import uk.gov.hmrc.trustsstore.services.TasksService

import scala.concurrent.Future


class MaintainTaskListControllerSpec extends BaseSpec {

  private val service: TasksService = mock[TasksService]

  lazy val application: Application = applicationBuilder().overrides(
    bind[TasksService].toInstance(service)
  ).build()

  override def beforeEach(): Unit = {
    Mockito.reset(service)
  }

  "invoking GET /maintain/tasks" - {

    "must return OK and the completed Tasks" in {
      val request = FakeRequest(GET, routes.MaintainTaskListController.get("utr").url)

      val tasks = Task(
        trustees = true,
        beneficiaries = false,
        settlors = false,
        other = false,
        protectors = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
    }

  }

  "invoking POST /maintain/task/trustees" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeTrustees("utr").url)

      val tasksCompletedSoFar = Task(
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false
      )

      val updatedTasks = Task(
        trustees = true,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

}
