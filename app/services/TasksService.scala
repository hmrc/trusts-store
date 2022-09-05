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

///*
// * Copyright 2022 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package services
//
//import models.tasks.Task.Task
//import models.tasks.TaskStatus.TaskStatus
//import models.tasks.{Task, Tasks}
//import play.api.libs.json.{JsValue, Json}
//import repositories.TasksRepository
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//trait TasksService {
//
//  val tasksRepository: TasksRepository
//
//  def get(internalId: String, identifier: String, sessionId: String): Future[Tasks] = {
//    tasksRepository.get(internalId, identifier, sessionId) map {
//      case Some(cache) => cache.task
//      case None => Tasks()
//    }
//  }
//
//  def set(internalId: String, identifier: String, sessionId: String, updated: Tasks): Future[Tasks] = {
//    tasksRepository.set(internalId, identifier, sessionId, updated).map(_ => updated)
//  }
//
//  def reset(internalId: String, identifier: String, sessionId: String): Future[Boolean] = {
//    tasksRepository.reset(internalId, identifier, sessionId)
//  }
//
//  def modifyTask(internalId: String, identifier: String, sessionId: String, update: Task, taskStatus: TaskStatus): Future[JsValue] = {
//    for {
//      tasks <- get(internalId, identifier, sessionId)
//      updatedTasks <- Future.successful {
//        update match {
//          case Task.TrustDetails => tasks.copy(trustDetails = taskStatus)
//          case Task.Assets => tasks.copy(assets = taskStatus)
//          case Task.TaxLiability => tasks.copy(taxLiability = taskStatus)
//          case Task.Trustees => tasks.copy(trustees = taskStatus)
//          case Task.Beneficiaries => tasks.copy(beneficiaries = taskStatus)
//          case Task.Protectors => tasks.copy(protectors = taskStatus)
//          case Task.Settlors => tasks.copy(settlors = taskStatus)
//          case Task.OtherIndividuals => tasks.copy(other = taskStatus)
//        }
//      }
//      savedTasks <- set(internalId, identifier, sessionId, updatedTasks)
//    } yield Json.toJson(savedTasks)
//  }
//
//}