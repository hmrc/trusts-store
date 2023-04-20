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

package services

import models.claim_a_trust.TrustClaim
import models.claim_a_trust.responses._
import play.api.Logging
import repositories.ClaimedTrustsRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ClaimedTrustsService @Inject() (private val claimedTrustsRepository: ClaimedTrustsRepository)(implicit
  ec: ExecutionContext
) extends Logging {

  def get(internalId: String): Future[ClaimedTrustResponse] =
    claimedTrustsRepository.get(internalId) map {
      case Some(trustClaim) =>
        GetClaimFound(trustClaim)
      case None             =>
        GetClaimNotFound
    }

  def store(
    internalId: String,
    identifier: Option[String],
    maybeManagedByAgent: Option[Boolean],
    maybeTrustLocked: Option[Boolean]
  )(implicit hc: HeaderCarrier): Future[ClaimedTrustResponse] = {

    val trustClaim = (identifier, maybeManagedByAgent, maybeTrustLocked) match {
      case (Some(id), Some(managedByAgent), None) =>
        logger.info(s"[store][Session ID: ${Session.id(hc)}] TrustClaim is not locked")
        Some(TrustClaim(internalId, id, managedByAgent))

      case (Some(id), Some(managedByAgent), Some(maybeTrustLocked)) =>
        if (maybeTrustLocked) {
          logger.info(s"[store][Session ID: ${Session.id(hc)}] TrustClaim is locked")
        }
        Some(TrustClaim(internalId, id, managedByAgent, maybeTrustLocked))

      case _ =>
        None
    }

    trustClaim match {
      case Some(tc) =>
        claimedTrustsRepository.store(tc).map {
          StoreSuccessResponse
        }
      case None     => Future.successful(StoreParsingError)
    }
  }

}
