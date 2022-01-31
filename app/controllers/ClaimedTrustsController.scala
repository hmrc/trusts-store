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

import controllers.actions.IdentifierAction
import models.claim_a_trust.responses.ClaimedTrustResponse._
import models.claim_a_trust.responses._
import models.responses.ErrorResponse
import models.responses.ErrorResponse._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.ClaimedTrustsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class ClaimedTrustsController @Inject()(
	cc: ControllerComponents,
 	service: ClaimedTrustsService,
	authAction: IdentifierAction)(implicit ec: ExecutionContext) extends BackendController(cc) {

	def get(): Action[AnyContent] = authAction.async {
		implicit request =>

			service.get(request.internalId) map {
				case GetClaimFound(trustClaim) =>
					Ok(trustClaim.toResponse)
				case GetClaimNotFound =>
					NotFound(Json.toJson(ErrorResponse(NOT_FOUND, CLAIM_TRUST_UNABLE_TO_LOCATE)))
			}
	}

	def store(): Action[JsValue] = authAction.async(parse.tolerantJson) {
		implicit request =>

			val identifier = (request.body \ "id").asOpt[String]
			val maybeManagedByAgent = (request.body \ "managedByAgent").asOpt[Boolean]
			val maybeTrustLocked = (request.body \ "trustLocked").asOpt[Boolean]
			val internalId = request.internalId

			service.store(internalId, identifier, maybeManagedByAgent, maybeTrustLocked) map {
				case StoreSuccessResponse(trustClaim) =>
					Created(trustClaim.toResponse)
				case StoreParsingError =>
					BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, CLAIM_TRUST_UNABLE_TO_PARSE)))
				case StoreErrorsResponse(storageErrors) =>
					InternalServerError(Json.toJson(ErrorResponse(INTERNAL_SERVER_ERROR, UNABLE_TO_STORE, Some(storageErrors.toJson))))
			}
	}

}