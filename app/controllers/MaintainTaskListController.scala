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
import models.Operation.{Complete, InProgress}
import models.Task
import models.maintain.Tasks
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
	authAction: IdentifierAction)(implicit ec: ExecutionContext) extends BackendController(cc) {

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

	def completeTrustDetails(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.TrustDetails, Complete).map(Ok(_))
	}

	def completeAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Assets, Complete).map(Ok(_))
	}

	def completeTaxLiability(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.TaxLiability, Complete).map(Ok(_))
	}

	def completeTrustees(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Trustees, Complete).map(Ok(_))
	}

	def completeBeneficiaries(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Beneficiaries, Complete).map(Ok(_))
	}

	def completeSettlors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Settlors, Complete).map(Ok(_))
	}

	def completeProtectors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Protectors, Complete).map(Ok(_))
	}

	def completeOtherIndividuals(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.OtherIndividuals, Complete).map(Ok(_))
	}

	def inProgressAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			service.modifyTask(request.internalId, identifier, Task.Assets, InProgress).map(Ok(_))
	}
}
