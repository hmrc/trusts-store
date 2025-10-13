/*
 * Copyright 2025 HM Revenue & Customs
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

package scheduler

import jakarta.inject.Inject
import models.UpdatedCounterValues
import org.apache.pekko.stream.{ActorAttributes, Materializer}
import org.apache.pekko.stream.scaladsl.{Keep, Sink, SinkQueueWithCancel, Source}
import org.bson.types.ObjectId
import play.api.{Configuration, Logger}
import repositories.MaintainTasksRepository

import scala.concurrent.duration.FiniteDuration

class SchedulerForMaintainTasksRepo @Inject() (maintainTasksRepository: MaintainTasksRepository, config: Configuration)(
  implicit mat: Materializer
) extends WorkerConfig {

  private val logger                       = Logger(this.getClass)
  private val initialDelay: FiniteDuration = durationValueFromConfig("schedulers.initial-delay", config)
  private val interval: FiniteDuration     = durationValueFromConfig("schedulers.interval ", config)
  private val queryLimit: Int              = config.get[Int]("schedulers.queryLimit")

  val tap: SinkQueueWithCancel[Unit] = {
    logger.info("[SchedulerForMaintainTasksRepo][Tap] init")
    Source
      .tick(initialDelay, interval, fixBadUpdatedAt(queryLimit))
      .flatMapConcat(identity)
      .wireTapMat(Sink.queue())(Keep.right)
      .toMat(Sink.ignore)(Keep.left)
      .withAttributes(ActorAttributes.supervisionStrategy(supervisionStrategy))
      .run()

  }

  def fixBadUpdatedAt(limit: Int): Source[Unit, _] = {
    logger.info(
      s"started [SchedulerForMaintainTasksRepo][fixBadUpdatedAt] [$maintainTasksRepository] method with limit = $limit"
    )
    Source
      .fromPublisher(maintainTasksRepository.getAllInvalidDateDocuments(limit = limit))
      .fold(List.empty[ObjectId])((acc, id) => id :: acc)
      .mapAsync(parallelism = 1) { ids =>
        if (ids.isEmpty) {

          scala.concurrent.Future
            .successful(UpdatedCounterValues(0, 0, 0))
            .map(_.report(maintainTasksRepository.className))(mat.executionContext)

        } else {

          maintainTasksRepository
            .updateAllInvalidDateDocuments(ids)
            .map(_.report(maintainTasksRepository.className))(mat.executionContext)
        }
      }
      .map { repo =>
        logger.info(s"[SchedulerForMaintainTasksRepo][fixBadUpdatedAt] ended $repo")
        repo
      }

  }

}
