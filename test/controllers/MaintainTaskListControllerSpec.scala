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
import services.MaintainTasksService
import utils.Session

import scala.concurrent.Future

class MaintainTaskListControllerSpec extends BaseSpec {

  private val service: MaintainTasksService = mock[MaintainTasksService]

  lazy val application: Application = applicationBuilder()
    .overrides(
      bind[MaintainTasksService].toInstance(service)
    )
    .build()

  override def beforeEach(): Unit =
    Mockito.reset(service)

  private val sessionId: String = Session.id(hc)

  "invoking GET /maintain/tasks" should {

    "return OK and the completed Tasks" in {
      val request = FakeRequest(GET, routes.MaintainTaskListController.get("utr").url)

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

  "invoking POST /maintain/tasks" should {

    "return OK and the completed Tasks for valid tasks" in {
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

      val request = FakeRequest(POST, routes.MaintainTaskListController.set("utr").url).withBody(Json.toJson(tasks))

      when(service.set(any(), any(), any(), any())).thenReturn(Future.successful(tasks))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.toJson(tasks)
      verify(service).set("id", "utr", sessionId, tasks)

    }
    "return BAD_REQUEST for invalid JSON" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.set("utr").url).withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking DELETE /maintain/tasks" should {

    "return OK" in {

      val request = FakeRequest(DELETE, routes.MaintainTaskListController.reset("utr").url)

      when(service.reset(any(), any(), any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustBe Status.OK
      verify(service).reset("id", "utr", sessionId)

    }

  }

  "invoking POST /maintain/task/update-trust-details" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateTrustDetailsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(trustDetails = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.TrustDetails), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateTrustDetailsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-assets" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateAssetsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(assets = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Assets), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateAssetsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-tax-liability" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateTaxLiabilityStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(taxLiability = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.TaxLiability), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateTaxLiabilityStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-trustees" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateTrusteesStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(trustees = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Trustees), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateTrusteesStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-beneficiaries" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateBeneficiariesStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(beneficiaries = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Beneficiaries), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateBeneficiariesStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-settlors" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateSettlorsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(settlors = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Settlors), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateSettlorsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-protectors" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request = FakeRequest(POST, routes.MaintainTaskListController.updateProtectorsStatus("identifier").url)
          .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(protectors = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.Protectors), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateProtectorsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }

  "invoking POST /maintain/task/update-other-individuals" should {

    "return Ok and the updated tasks" in {

      forAll(arbitrary[TaskStatus]) { taskStatus =>
        reset(service)

        val request =
          FakeRequest(POST, routes.MaintainTaskListController.updateOtherIndividualsStatus("identifier").url)
            .withBody(Json.toJson(taskStatus))

        val tasksBeforeUpdate = Tasks()

        val tasksAfterUpdate = tasksBeforeUpdate.copy(other = taskStatus)

        when(service.modifyTask(any(), any(), any(), any(), any()))
          .thenReturn(Future.successful(Json.toJson(tasksAfterUpdate)))

        val result = route(application, request).value

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(tasksAfterUpdate)

        verify(service)
          .modifyTask(any(), any(), any(), ArgumentMatchers.eq(Task.OtherIndividuals), ArgumentMatchers.eq(taskStatus))
      }
    }

    "return Bad Request when invalid request body" in {

      val request = FakeRequest(POST, routes.MaintainTaskListController.updateOtherIndividualsStatus("identifier").url)
        .withBody(Json.obj())

      val result = route(application, request).value

      status(result) mustBe Status.BAD_REQUEST
    }

  }
}
