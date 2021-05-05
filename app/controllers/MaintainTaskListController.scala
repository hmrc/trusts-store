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
import models.maintain.Task
import play.api.libs.json._
import play.api.mvc._
import services.TasksService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait UpdateOperation
case object UpdateTrustDetails extends UpdateOperation
case object UpdateAssets extends UpdateOperation
case object UpdateTaxLiability extends UpdateOperation
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

	def reset(identifier: String): Action[AnyContent] = authAction.async {
		request =>
			service.reset(request.internalId, identifier).map(_ => Ok)
	}

	private def updateTask(internalId: String, identifier: String, update: UpdateOperation): Future[Result] = for {
		tasks <- service.get(internalId, identifier)
		updatedTasks <- Future.successful {
			update match {
				case UpdateTrustDetails => tasks.copy(trustDetails = true)
				case UpdateAssets => tasks.copy(assets = true)
				case UpdateTaxLiability => tasks.copy(taxLiability = true)
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

	def completeTrustDetails(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateTrustDetails)
	}

	def completeAssets(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateAssets)
	}

	def completeTaxLiability(identifier: String): Action[AnyContent] = authAction.async {
		implicit request =>
			updateTask(request.internalId, identifier, UpdateTaxLiability)
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
