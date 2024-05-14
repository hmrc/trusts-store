/*
 * Copyright 2024 HM Revenue & Customs
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

package repositories

import config.AppConfig
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class RepositoriesBaseSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with BeforeAndAfterAll {

  def cleanData(collection: MongoCollection[_]): Unit =
    collection.deleteMany(BsonDocument()).toFuture().futureValue

  val application: Application = new GuiceApplicationBuilder()
    .configure(
      "auditing.enabled" -> false,
      "metrics.enabled"  -> false,
      "mongodb.uri"      -> "mongodb://localhost:27017/trusts-store-test-it"
    )
    .build()
  val appConfig: AppConfig     = application.injector.instanceOf[AppConfig]

}
