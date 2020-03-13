package uk.gov.hmrc.trustsstore.suite

import org.scalatest.TestSuite
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait MongoSuite extends IntegrationPatience {
  self: TestSuite with PatienceConfiguration =>

  // Database boilerplate
  private val connectionString = "mongodb://localhost:27017/trusts-store-integration"

  def getDatabase(connection: MongoConnection) = {
    connection.database("trusts-store-integration")
  }

  def getConnection(application: Application) = {
    val mongoDriver = application.injector.instanceOf[ReactiveMongoApi]

    lazy val connection = for {
      uri <- MongoConnection.parseURI(connectionString)
      connection <- mongoDriver.driver.connection(uri, true)
    } yield connection
    connection
  }

  def dropTheDatabase(connection: MongoConnection): Unit = {
    Await.result(getDatabase(connection).flatMap(_.drop()), Duration.Inf)
  }

  val application : Application = new GuiceApplicationBuilder()
    .configure(Seq(
      "mongodb.uri" -> connectionString,
      "metrics.enabled" -> false,
      "auditing.enabled" -> false,
      "mongo-async-driver.akka.log-dead-letters" -> 0
    ): _*)
    .build()

}
