package repositories

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import models.maintain.Task
import suite.MongoSuite

import scala.language.implicitConversions

class TasksRepositorySpec extends FreeSpec with MustMatchers
  with ScalaFutures with OptionValues with MongoSuite {

  "a tasks repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

    "must return None when no cache exists" in {
      running(application) {

        getConnection(application).map{ connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[TasksRepository]

          repository.get(internalId, "utr").futureValue mustBe None
        }
      }
    }

    "must set an updated Task and return one that exists for that user" in {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[TasksRepository]

          val task = Task(trustees = true, settlors = false, protectors = false, beneficiaries = false, other = false)

          val result = repository.set(internalId, "1234567890", task).futureValue

          result mustBe true

          repository.get(internalId, "1234567890").futureValue.value.task mustBe task

          dropTheDatabase(connection)
        }

      }
    }

  }
}
