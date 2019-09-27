package uk.gov.hmrc.trustsstore.suite

import com.typesafe.config.ConfigFactory
import org.scalatest.TestSuite
import play.api.{Application, Configuration}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import uk.gov.hmrc.trustsstore.repositories.ClaimedTrustsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoSuite {

  private lazy val config = Configuration(ConfigFactory.load(System.getProperty("config.resource")))

  private lazy val parsedUri = Future.fromTry {
    MongoConnection.parseURI(config.get[String]("mongodb.uri"))
  }

  lazy val connection =
    parsedUri.map(MongoDriver().connection)
}

trait MongoSuite {
  self: TestSuite =>

  def started(app: Application): Future[_] = {

    val claimedTrustsRepository = app.injector.instanceOf[ClaimedTrustsRepository]

    val services = Seq(claimedTrustsRepository.started)

    Future.sequence(services)
  }

  def database: Future[DefaultDB] = {
    for {
      uri        <- MongoSuite.parsedUri
      connection <- MongoSuite.connection
      database   <- connection.database(uri.db.get)
    } yield database
  }
}
