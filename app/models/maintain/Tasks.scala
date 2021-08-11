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

package models.maintain

import models.TaskStatus.{InProgress, Status}
import play.api.libs.json.{Format, Json}

case class Tasks(trustDetails: Status,
                 assets: Status,
                 taxLiability: Status,
                 trustees: Status,
                 beneficiaries: Status,
                 settlors: Status,
                 protectors: Status,
                 other: Status)

object Tasks {

  def apply(): Tasks = Tasks(
    trustDetails = InProgress,
    assets = InProgress,
    taxLiability = InProgress,
    trustees = InProgress,
    beneficiaries = InProgress,
    settlors = InProgress,
    protectors = InProgress,
    other = InProgress
  )

  implicit val formats: Format[Tasks] = Json.format[Tasks]

}
