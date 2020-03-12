package uk.gov.hmrc.trustsstore.repositories

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import suite.FailOnUnindexedQueries
import uk.gov.hmrc.trustsstore.models.maintain.{Task, TaskCache}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class TasksRepositorySpec extends FreeSpec with MustMatchers with FailOnUnindexedQueries with IntegrationPatience
  with ScalaFutures with OptionValues with Inside with EitherValues with Eventually {

  private lazy val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()

  "a tasks repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

    "must return None when no cache exists" in {

      database.map(_.drop()).futureValue

      val application = appBuilder.build()

      running(application) {

        val repository = application.injector.instanceOf[TasksRepository]

        started(application).futureValue

        repository.get(internalId, "utr").futureValue mustBe None

      }
    }

    "must set an updated Task and return one that exists for that user" in {
      database.map(_.drop()).futureValue

      val application = appBuilder.build()

      running(application) {

        val repository = application.injector.instanceOf[TasksRepository]

        started(application).futureValue

        val cache = TaskCache(internalId, "1234567890", task = Task())

        val task = Task(trustees = false, settlors = false, protectors = false, beneficiaries = false, other = false)

        val result = repository.set(internalId, "1234567890", task).futureValue

        result mustBe true

        repository.get(internalId, "1234567890").futureValue.value.task mustBe task

      }
    }

  }
}
