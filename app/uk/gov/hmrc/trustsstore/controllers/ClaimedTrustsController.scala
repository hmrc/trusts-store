/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.trustsstore.controllers.actions.IdentifierAction
import uk.gov.hmrc.trustsstore.models.claim_a_trust.responses._
import uk.gov.hmrc.trustsstore.services.ClaimedTrustsService

import scala.concurrent.ExecutionContext

@Singleton()
class ClaimedTrustsController @Inject()(
	cc: ControllerComponents,
 	service: ClaimedTrustsService,
	authAction: IdentifierAction)(implicit ec: ExecutionContext) extends BackendController(cc) {

	def get(): Action[AnyContent] = authAction.async {
		implicit request =>

			service.get(request.internalId) map {
				case GetClaimFoundResponse(trustClaim) =>
					Ok(Json.toJson(trustClaim))
				case GetClaimNotFoundResponse(error) =>
					NotFound(error)
			}
	}

	def store(): Action[JsValue] = authAction.async(parse.tolerantJson) {
		implicit request =>

			val maybeUtr = (request.body \ "utr").asOpt[String]
			val maybeManagedByAgent = (request.body \ "managedByAgent").asOpt[Boolean]
			val internalId = request.internalId

			service.store(internalId, maybeUtr, maybeManagedByAgent) map {
				case StoreSuccessResponse(trustClaim) =>
					Created(Json.toJson(trustClaim))
				case StoreParsingErrorResponse(error) =>
					BadRequest(error)
				case StoreErrorsResponse(errors) =>
					InternalServerError(errors.toJson)
			}
	}

}