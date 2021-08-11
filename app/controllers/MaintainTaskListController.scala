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
import models.TaskStatus.{Completed, InProgress}
import models.{Task, TaskStatus}
import models.maintain.Tasks
import play.api.Logging
import play.api.libs.json._
import play.api.mvc._
import services.TasksService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class MaintainTaskListController @Inject()(
																						cc: ControllerComponents,
																						service: TasksService,
																						authAction: IdentifierAction
																					)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

	def get(identifier: String): Action[AnyContent] = authAction.async {
		request =>

			service.get(request.internalId, identifier).map {
				task =>
					Ok(Json.toJson(task))
			}
	}

	def set(identifier: String): Action[JsValue] = authAction.async(parse.json) {
		request =>
			request.body.validate[Tasks] match {
				case JsSuccess(tasks, _) =>
					service.set(request.internalId, identifier, tasks).map {
						updated => Ok(Json.toJson(updated))
					}
				case _ => Future.successful(BadRequest)
			}
	}

	def reset(identifier: String): Action[AnyContent] = authAction.async {
		request =>
			service.reset(request.internalId, identifier).map(_ => Ok)
	}

	@Deprecated
	def completeTrustDetails(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.TrustDetails, Completed).map(Ok(_))
	}

	@Deprecated
	def completeAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Assets, Completed).map(Ok(_))
	}

	@Deprecated
	def completeTaxLiability(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.TaxLiability, Completed).map(Ok(_))
	}

	@Deprecated
	def completeTrustees(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Trustees, Completed).map(Ok(_))
	}

	@Deprecated
	def completeBeneficiaries(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Beneficiaries, Completed).map(Ok(_))
	}

	@Deprecated
	def completeSettlors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Settlors, Completed).map(Ok(_))
	}

	@Deprecated
	def completeProtectors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Protectors, Completed).map(Ok(_))
	}

	@Deprecated
	def completeOtherIndividuals(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.OtherIndividuals, Completed).map(Ok(_))
	}

	@Deprecated
	def inProgressAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Assets, InProgress).map(Ok(_))
	}

	def updateTrustDetailsStatus(identifier: String): Action[JsValue] = authAction.async(parse.json) {
		implicit request =>
			request.body.validate[TaskStatus.Value] match {
				case JsSuccess(taskStatus, _) =>
					service.modifyTask(request.internalId, identifier, Task.TrustDetails, taskStatus).map(Ok(_))
				case JsError(errors) =>
					logger.error(s"[Identifier: $identifier] Error validating request body as TaskStatus: $errors")
					Future.successful(BadRequest)
			}
	}
}
