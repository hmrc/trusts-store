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

package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import controllers.actions.IdentifierAction
import models.maintain.Task
import services.TasksService

import scala.concurrent.{ExecutionContext, Future}

sealed trait UpdateOperation
case object UpdateTrustees extends UpdateOperation
case object UpdateBeneficiaries extends UpdateOperation
case object UpdateSettlors extends UpdateOperation
case object UpdateProtectors extends UpdateOperation
case object UpdateOtherIndividuals extends UpdateOperation

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
			request.body.validate[Task] match {
				case JsSuccess(tasks, _) =>
					service.set(request.internalId, identifier, tasks).map {
						updated => Ok(Json.toJson(updated))
					}
				case _ => Future.successful(BadRequest)
			}
	}

	private def updateTask(internalId: String, identifier: String, update: UpdateOperation) = for {
		tasks <- service.get(internalId, identifier)
		updatedTasks <- Future.successful {
			update match {
				case UpdateTrustees => tasks.copy(trustees = true)
				case UpdateBeneficiaries => tasks.copy(beneficiaries = true)
				case UpdateProtectors => tasks.copy(protectors = true)
				case UpdateSettlors => tasks.copy(settlors = true)
				case UpdateOtherIndividuals => tasks.copy(other = true)
			}
		}
		savedTasks <- service.set(internalId, identifier, updatedTasks)
	} yield {
		Ok(Json.toJson(savedTasks))
	}

	def completeTrustees(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateTrustees)
	}

	def completeBeneficiaries(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateBeneficiaries)
	}

	def completeSettlors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateSettlors)
	}

	def completeProtectors(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateProtectors)
	}

	def completeOtherIndividuals(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateOtherIndividuals)
	}

}