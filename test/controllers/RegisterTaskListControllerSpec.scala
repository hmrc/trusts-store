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

package controllers

import base.BaseSpec
import models.tasks.TaskStatus._
import models.tasks.{Task, Tasks}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalacheck.Arbitrary.arbitrary
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.RegisterTasksService
import utils.Session

import scala.concurrent.Future

class RegisterTaskListControllerSpec extends BaseSpec {

  private val service: RegisterTasksService = mock[RegisterTasksService]

  lazy val application: Application = applicationBuilder().overrides(
    bind[RegisterTasksService].toInstance(service)
  ).build()

  override def beforeEach(): Unit = {
    Mockito.reset(service)
  }

  private val sessionId: String = Session.id(hc)

  "invoking GET /register/tasks" - {

    "must return OK and the completed Tasks" in {
      val request = FakeRequest(GET, routes.RegisterTaskListController.get("draftId").url)

      val tasks = Tasks(
        trustDetails = InProgress,
        assets = InProgress,
        taxLiability = InProgress,
        trustees = Completed,
        beneficiaries = InProgress,
        settlors = InProgress,
        other = InProgress,
        protectors = InProgress
      )

      when(service.get(any(), any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
    }

  }

  "invoking POST /register/tasks" - {

    "must return OK and the completed Tasks for valid tasks" in {
      val tasks = Tasks(
        trustDetails = InProgress,
        assets = InProgress,
        taxLiability = InProgress,
        trustees = Completed,
        beneficiaries = InProgress,
        settlors = Completed,
        other = InProgress,
        protectors = InProgress
      )

      val request = FakeRequest(POST, routes.RegisterTaskListController.set("draftId").url).withBody(Json.toJson(tasks))

      when(service.set(any(), any(), any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
      verify(service).set("id", "draftId", sessionId, tasks)

    }
    "must return BAD_REQUEST for invalid JSON" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.set("draftId").url).withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking DELETE /register/tasks" - {

    "must return OK" in {

      val request = FakeRequest(DELETE, routes.RegisterTaskListController.reset("draftId").url)

      when(service.reset(any(), any(), any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      verify(service).reset("id", "draftId", sessionId)

    }

  }

  "invoking POST /register/task/update-trust-details" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateTrustDetailsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(trustDetails = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.TrustDetails), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateTrustDetailsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-assets" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateAssetsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(assets = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Assets), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateAssetsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-tax-liability" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateTaxLiabilityStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(taxLiability = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.TaxLiability), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateTaxLiabilityStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-trustees" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateTrusteesStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(trustees = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Trustees), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateTrusteesStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-beneficiaries" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateBeneficiariesStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(beneficiaries = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Beneficiaries), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateBeneficiariesStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-settlors" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateSettlorsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(settlors = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Settlors), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateSettlorsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-protectors" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateProtectorsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(protectors = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Protectors), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateProtectorsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /register/task/update-other-individuals" - {

    "must return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>

        reset(service)

        val request = FakeRequest(POST, routes.RegisterTaskListController.updateOtherIndividualsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(other = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any())).thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service).modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.OtherIndividuals), ArgumentMatchers.eq(taskStatus))
      }
    }

    "must return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.RegisterTaskListController.updateOtherIndividualsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }
}
