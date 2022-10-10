package repositories

import config.AppConfig
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Play}

class RepositoriesBaseSpec extends AnyWordSpec with Matchers with ScalaFutures with OptionValues with BeforeAndAfterAll {

  def cleanData(collection: MongoCollection[_]): Unit = {
    collection.deleteMany(BsonDocument()).toFuture().futureValue
  }

  val application: Application = new GuiceApplicationBuilder()
    .build()
  val appConfig: AppConfig = application.injector.instanceOf[AppConfig]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    Play.start(application)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    Play.stop(application)
  }
}
