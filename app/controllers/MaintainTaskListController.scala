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
import models.tasks.Task
import models.tasks.TaskStatus.{Completed, InProgress}
import play.api.mvc._
import services.MaintainTasksService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class MaintainTaskListController @Inject()(cc: ControllerComponents,
																					 override val tasksService: MaintainTasksService,
																					 override val authAction: IdentifierAction)
																					(override implicit val ec: ExecutionContext)
	extends TaskListController(cc) {

	@Deprecated
	def completeTrustDetails(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.TrustDetails, Completed).map(Ok(_))
	}

	@Deprecated
	def completeAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Assets, Completed).map(Ok(_))
	}

	@Deprecated
	def completeTaxLiability(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.TaxLiability, Completed).map(Ok(_))
	}

	@Deprecated
	def completeTrustees(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Trustees, Completed).map(Ok(_))
	}

	@Deprecated
	def completeBeneficiaries(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Beneficiaries, Completed).map(Ok(_))
	}

	@Deprecated
	def completeSettlors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Settlors, Completed).map(Ok(_))
	}

	@Deprecated
	def completeProtectors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Protectors, Completed).map(Ok(_))
	}

	@Deprecated
	def completeOtherIndividuals(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.OtherIndividuals, Completed).map(Ok(_))
	}

	@Deprecated
	def inProgressAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			tasksService.modifyTask(request.internalId, identifier, Task.Assets, InProgress).map(Ok(_))
	}
}
