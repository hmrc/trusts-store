/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions.IdentifierAction
import play.api.mvc._
import services.MaintainTasksService
import javax.inject.{Inject, Singleton}
import models.tasks.Task.Task
import models.tasks.TaskStatus.TaskStatus
import models.tasks.{Task, Tasks}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class MaintainTaskListController @Inject() (
  cc: ControllerComponents,
  val tasksService: MaintainTasksService,
  val authAction: IdentifierAction
)(implicit val ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def get(identifier: String): Action[AnyContent] = authAction.async { implicit request =>
    tasksService.get(request.internalId, identifier, Session.id(hc)).map { task =>
      Ok(Json.toJson(task))
    }
  }

  def set(identifier: String): Action[JsValue] = authAction.async(parse.json) { implicit request =>
    request.body.validate[Tasks] match {
      case JsSuccess(tasks, _) =>
        tasksService.set(request.internalId, identifier, Session.id(hc), tasks).map { updated =>
          Ok(Json.toJson(updated))
        }
      case _                   => Future.successful(BadRequest)
    }
  }

  def reset(identifier: String): Action[AnyContent] = authAction.async { implicit request =>
    tasksService.reset(request.internalId, identifier, Session.id(hc)).map(_ => Ok)
  }

  def updateTrustDetailsStatus(identifier: String): Action[JsValue]     = updateTaskStatus(identifier, Task.TrustDetails)
  def updateAssetsStatus(identifier: String): Action[JsValue]           = updateTaskStatus(identifier, Task.Assets)
  def updateTaxLiabilityStatus(identifier: String): Action[JsValue]     = updateTaskStatus(identifier, Task.TaxLiability)
  def updateTrusteesStatus(identifier: String): Action[JsValue]         = updateTaskStatus(identifier, Task.Trustees)
  def updateBeneficiariesStatus(identifier: String): Action[JsValue]    = updateTaskStatus(identifier, Task.Beneficiaries)
  def updateSettlorsStatus(identifier: String): Action[JsValue]         = updateTaskStatus(identifier, Task.Settlors)
  def updateProtectorsStatus(identifier: String): Action[JsValue]       = updateTaskStatus(identifier, Task.Protectors)
  def updateOtherIndividualsStatus(identifier: String): Action[JsValue] =
    updateTaskStatus(identifier, Task.OtherIndividuals)

  private def updateTaskStatus(identifier: String, task: Task): Action[JsValue] = authAction.async(parse.json) {
    implicit request =>
      request.body.validate[TaskStatus] match {
        case JsSuccess(taskStatus, _) =>
          tasksService.modifyTask(request.internalId, identifier, Session.id(hc), task, taskStatus).map(Ok(_))
        case JsError(errors)          =>
          logger.error(s"[Identifier: $identifier] Error validating request body as TaskStatus: $errors")
          Future.successful(BadRequest)
      }
  }

}
