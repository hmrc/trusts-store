package repositories

import models.tasks.TaskStatus._
import models.tasks.Tasks
import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import suite.MongoSuite
import org.scalatest.matchers.must.Matchers

import scala.concurrent.ExecutionContext.Implicits._

class RegisterTasksRepositorySpec extends AnyFreeSpec with Matchers
  with ScalaFutures with OptionValues with MongoSuite {

  "a register tasks repository" - {

    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"
    val draftId = "draftId"
    val sessionId: String = "session-d41ebbc3-38bc-4276-86da-5533eb878e37"

    "useSessionId must be false " in {
      val repository = application.injector.instanceOf[RegisterTasksRepository]
      val test = repository.useSessionId
      test mustBe false
    }

    "must return None when no cache exists" ignore {
      running(application) {

        getConnection(application).map{ connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[RegisterTasksRepository]

          repository.get(internalId, draftId, sessionId).futureValue mustBe None
        }
      }
    }

    "must set an updated Task and return one that exists for that user" ignore {
      running(application) {

        getConnection(application).map { connection =>
          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[RegisterTasksRepository]

          val task = Tasks()

          val result = repository.set(internalId, draftId, sessionId, task).futureValue

          result mustBe true

          repository.get(internalId, draftId, sessionId).futureValue.value.task mustBe task

          dropTheDatabase(connection)
        }

      }
    }

    "must reset the task list so every task is incomplete" ignore {
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

          repository.set(internalId, draftId, sessionId, tasksCompleted).futureValue

          repository.get(internalId, draftId, sessionId).futureValue.value.task mustBe tasksCompleted

          val result = repository.reset(internalId, draftId, sessionId).futureValue

          result mustBe true

          repository.get(internalId, draftId, sessionId).futureValue.value.task mustBe Tasks()

          dropTheDatabase(connection)
        }

      }
    }
  }
}
