/*
 * Copyright 2026 HM Revenue & Customs
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

package models.tasks

import models.tasks.TaskStatus.{NotStarted, TaskStatus}
import play.api.libs.json.{Format, Json}

case class Tasks(
  trustDetails: TaskStatus,
  assets: TaskStatus,
  taxLiability: TaskStatus,
  trustees: TaskStatus,
  beneficiaries: TaskStatus,
  settlors: TaskStatus,
  protectors: TaskStatus,
  other: TaskStatus
)

object Tasks {

  def apply(): Tasks = Tasks(
    trustDetails = NotStarted,
    assets = NotStarted,
    taxLiability = NotStarted,
    trustees = NotStarted,
    beneficiaries = NotStarted,
    settlors = NotStarted,
    protectors = NotStarted,
    other = NotStarted
  )

  implicit val formats: Format[Tasks] = Json.format[Tasks]

}
