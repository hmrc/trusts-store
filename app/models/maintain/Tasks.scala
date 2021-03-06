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

import play.api.libs.json.{Format, Json}

case class Tasks(trustDetails: Boolean,
                 assets: Boolean,
                 taxLiability: Boolean,
                 trustees: Boolean,
                 beneficiaries: Boolean,
                 settlors: Boolean,
                 protectors: Boolean,
                 other: Boolean)

object Tasks {

  def apply(): Tasks = Tasks(
    trustDetails = false,
    assets = false,
    taxLiability = false,
    trustees = false,
    beneficiaries = false,
    settlors = false,
    protectors = false,
    other = false
  )

  implicit val formats: Format[Tasks] = Json.format[Tasks]

}
