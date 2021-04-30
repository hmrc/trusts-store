package repositories

import models.maintain.Task
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import suite.MongoSuite

import scala.concurrent.ExecutionContext.Implicits._

class TasksRepositorySpec extends FreeSpec with MustMatchers
  with ScalaFutures with OptionValues with MongoSuite {

  "a tasks repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"
    val identifier = "1234567890"

    "must return None when no cache exists" in {
      running(application) {

        getConnection(application).map{ connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[TasksRepository]

          repository.get(internalId, identifier).futureValue mustBe None
        }
      }
    }

    "must set an updated Task and return one that exists for that user" in {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[TasksRepository]

          val task = Task(
            trustDetails = false,
            assets = false,
            taxLiability = false,
            trustees = true,
            settlors = false,
            protectors = false,
            beneficiaries = false,
            other = false,
            nonEeaCompany = false
          )

          val result = repository.set(internalId, identifier, task).futureValue

          result mustBe true

          repository.get(internalId, identifier).futureValue.value.task mustBe task

          dropTheDatabase(connection)
        }

      }
    }

    "must reset the task list so every task is incomplete" in {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[TasksRepository]

          val allTasksComplete = Task(
            trustDetails = true,
            assets = true,
            taxLiability = true,
            trustees = true,
            settlors = true,
            protectors = true,
            beneficiaries = true,
            other = true,
            nonEeaCompany = true
          )

          val allTasksIncomplete = Task(
            trustDetails = false,
            assets = false,
            taxLiability = false,
            trustees = false,
            settlors = false,
            protectors = false,
            beneficiaries = false,
            other = false,
            nonEeaCompany = false
          )

          repository.set(internalId, identifier, allTasksComplete).futureValue

          repository.get(internalId, identifier).futureValue.value.task mustBe allTasksComplete

          val result = repository.reset(internalId, identifier).futureValue

          result mustBe true

          repository.get(internalId, identifier).futureValue.value.task mustBe allTasksIncomplete

          dropTheDatabase(connection)
        }

      }
    }
  }
}
