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

import controllers.actions.IdentifierAction
import models.tasks.Task.Task
import models.tasks.{Task, TaskStatus, Tasks}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc._
import services.TasksService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

abstract class TaskListController(cc: ControllerComponents) extends BackendController(cc) with Logging {

	val tasksService: TasksService
	val authAction: IdentifierAction
	implicit val ec: ExecutionContext

	def get(identifier: String): Action[AnyContent] = authAction.async {
		request =>
			tasksService.get(request.internalId, identifier).map {
				task =>
					Ok(Json.toJson(task))
			}
	}

	def set(identifier: String): Action[JsValue] = authAction.async(parse.json) {
		request =>
			request.body.validate[Tasks] match {
				case JsSuccess(tasks, _) =>
					tasksService.set(request.internalId, identifier, tasks).map {
						updated => Ok(Json.toJson(updated))
					}
				case _ => Future.successful(BadRequest)
			}
	}

	def reset(identifier: String): Action[AnyContent] = authAction.async {
		request =>
			tasksService.reset(request.internalId, identifier).map(_ => Ok)
	}

	def updateTrustDetailsStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.TrustDetails)
	def updateAssetsStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.Assets)
	def updateTaxLiabilityStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.TaxLiability)
	def updateTrusteesStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.Trustees)
	def updateBeneficiariesStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.Beneficiaries)
	def updateSettlorsStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.Settlors)
	def updateProtectorsStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.Protectors)
	def updateOtherIndividualsStatus(identifier: String): Action[JsValue] = updateTaskStatus(identifier, Task.OtherIndividuals)

	private def updateTaskStatus(identifier: String, task: Task): Action[JsValue] = authAction.async(parse.json) {
		implicit request =>
			request.body.validate[TaskStatus.Value] match {
				case JsSuccess(taskStatus, _) =>
					tasksService.modifyTask(request.internalId, identifier, task, taskStatus).map(Ok(_))
				case JsError(errors) =>
					logger.error(s"[Identifier: $identifier] Error validating request body as TaskStatus: $errors")
					Future.successful(BadRequest)
			}
	}
}
