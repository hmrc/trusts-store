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

package controllers

import base.BaseSpec
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.maintain.Task
import services.TasksService

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
        trustDetails = false,
        trustees = true,
        beneficiaries = false,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
    }

  }

  "invoking POST /maintain/tasks" - {

    "must return OK and the completed Tasks for valid tasks" in {
      val tasks = Task(
        trustDetails = false,
        trustees = true,
        beneficiaries = false,
        settlors = true,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val request = FakeRequest(POST, routes.MaintainTaskListController.set("utr").url).withBody(Json.toJson(tasks))

      when(service.set(any(), any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
      verify(service).set("id", "utr", tasks)

    }
    "must return BAD_REQUEST for invalid JSON" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.set("utr").url).withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/trust-details" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeTrustDetails("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = true,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/task/trustees" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeTrustees("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = true,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/task/beneficiaries" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeBeneficiaries("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = false,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = true,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/task/protectors" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeProtectors("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = true,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/task/settlors" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeSettlors("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = true,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/task/others" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeOtherIndividuals("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = true,
        protectors = false,
        nonEeaCompany = false
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }

  "invoking POST /maintain/tasks/non-eea-company" - {

    "must return Ok and the completed tasks" in {
      val request = FakeRequest(POST, routes.MaintainTaskListController.completeNonEeaCompany("utr").url)

      val tasksCompletedSoFar = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = false
      )

      val updatedTasks = Task(
        trustDetails = false,
        trustees = false,
        beneficiaries = true,
        settlors = false,
        other = false,
        protectors = false,
        nonEeaCompany = true
      )

      when(service.get(any(), any())).thenReturn(Future.successful(tasksCompletedSoFar))
      when(service.set(any(), any(), any())).thenReturn(Future.successful(updatedTasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(updatedTasks)
    }

  }
}
