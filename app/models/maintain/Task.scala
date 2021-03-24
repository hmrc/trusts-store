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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Task(trustDetails: Boolean = false,
                trustees: Boolean = false,
                beneficiaries: Boolean = false,
                settlors: Boolean = false,
                protectors: Boolean = false,
                other: Boolean = false,
                nonEeaCompany: Boolean = false)

object Task {

  implicit val reads: Reads[Task] = (
    (__ \ 'trustDetails).readWithDefault[Boolean](true) and
      (__ \ 'trustees).read[Boolean] and
      (__ \ 'beneficiaries).read[Boolean] and
      (__ \ 'settlors).read[Boolean] and
      (__ \ 'protectors).read[Boolean] and
      (__ \ 'other).read[Boolean] and
      (__ \ 'nonEeaCompany).readWithDefault[Boolean](true)
    )(Task.apply _)

  implicit val writes: Writes[Task] = Json.writes[Task]

}
