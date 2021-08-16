package repositories

import models.tasks.TaskStatus._
import models.tasks.Tasks
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import suite.MongoSuite

import scala.concurrent.ExecutionContext.Implicits._

class RegisterTasksRepositorySpec extends FreeSpec with MustMatchers
  with ScalaFutures with OptionValues with MongoSuite {

  "a register tasks repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"
    val draftId = "draftId"

    "must return None when no cache exists" in {
      running(application) {

        getConnection(application).map{ connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[RegisterTasksRepository]

          repository.get(internalId, draftId).futureValue mustBe None
        }
      }
    }

    "must set an updated Task and return one that exists for that user" in {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[RegisterTasksRepository]

          val task = Tasks()

          val result = repository.set(internalId, draftId, task).futureValue

          result mustBe true

          repository.get(internalId, draftId).futureValue.value.task mustBe task

          dropTheDatabase(connection)
        }

      }
    }

    "must reset the task list so every task is incomplete" in {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[RegisterTasksRepository]

          val tasksCompleted = Tasks(
            trustDetails = Completed,
            assets = Completed,
            taxLiability = Completed,
            trustees = Completed,
            settlors = Completed,
            protectors = Completed,
            beneficiaries = Completed,
            other = Completed
          )

          repository.set(internalId, draftId, tasksCompleted).futureValue

          repository.get(internalId, draftId).futureValue.value.task mustBe tasksCompleted

          val result = repository.reset(internalId, draftId).futureValue

          result mustBe true

          repository.get(internalId, draftId).futureValue.value.task mustBe Tasks()

          dropTheDatabase(connection)
        }

      }
    }
  }
}
