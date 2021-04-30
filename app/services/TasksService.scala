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

import javax.inject.{Inject, Singleton}
import models.maintain.Task
import repositories.TasksRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class TasksService @Inject()(tasksRepository: TasksRepository)  {

  def get(internalId: String, identifier: String): Future[Task] = {
    tasksRepository.get(internalId, identifier) map {
      case Some(cache) => cache.task
      case None => Task()
    }
  }

  def set(internalId: String, identifier: String, updated: Task): Future[Task] = {
    tasksRepository.set(internalId, identifier, updated).map(_ => updated)
  }

  def reset(internalId: String, identifier: String): Future[Boolean] = {
    tasksRepository.reset(internalId, identifier)
  }

}