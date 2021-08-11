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

package services

import models.Status.Status
import models.Task
import models.Task.Task
import models.maintain.Tasks
import play.api.libs.json.{JsValue, Json}
import repositories.TasksRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class TasksService @Inject()(tasksRepository: TasksRepository)  {

  def get(internalId: String, identifier: String): Future[Tasks] = {
    tasksRepository.get(internalId, identifier) map {
      case Some(cache) => cache.task
      case None => Tasks()
    }
  }

  def set(internalId: String, identifier: String, updated: Tasks): Future[Tasks] = {
    tasksRepository.set(internalId, identifier, updated).map(_ => updated)
  }

  def reset(internalId: String, identifier: String): Future[Boolean] = {
    tasksRepository.reset(internalId, identifier)
  }

  def modifyTask(internalId: String, identifier: String, update: Task, status: Status): Future[JsValue] = {
    for {
      tasks <- get(internalId, identifier)
      updatedTasks <- Future.successful {
        update match {
          case Task.TrustDetails => tasks.copy(trustDetails = status)
          case Task.Assets => tasks.copy(assets = status)
          case Task.TaxLiability => tasks.copy(taxLiability = status)
          case Task.Trustees => tasks.copy(trustees = status)
          case Task.Beneficiaries => tasks.copy(beneficiaries = status)
          case Task.Protectors => tasks.copy(protectors = status)
          case Task.Settlors => tasks.copy(settlors = status)
          case Task.OtherIndividuals => tasks.copy(other = status)
        }
      }
      savedTasks <- set(internalId, identifier, updatedTasks)
    } yield Json.toJson(savedTasks)
  }

}
