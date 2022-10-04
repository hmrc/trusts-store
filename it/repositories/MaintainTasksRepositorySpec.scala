package repositories

import models.tasks.TaskStatus._
import models.tasks.Tasks
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext.Implicits.global

class MaintainTasksRepositorySpec extends RepositoriesBaseSpec with MongoSupport {

  val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"
  val identifier = "newId"
  val sessionId: String = "session-d41ebbc3-38bc-4276-86da-5533eb878e37"

  val defaultTask: Tasks = Tasks()

  lazy val repository: MaintainTasksRepository = new MaintainTasksRepository(mongoComponent, appConfig)

  "a maintain tasks repository" should {

    "return None when no cache exists" in {
      cleanData(repository.collection)

      repository.get(internalId, identifier, sessionId).futureValue mustBe None
    }

    "set an updated Task and return one that exists for that user" in {
      cleanData(repository.collection)

      repository.set(internalId, identifier, sessionId, defaultTask).futureValue.value mustBe defaultTask

      repository.get(internalId, identifier, sessionId).futureValue.value mustBe defaultTask
    }

    "reset the task list so every task is incomplete" in {
      cleanData(repository.collection)

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

      repository.set(internalId, identifier, sessionId, tasksCompleted).futureValue.value mustBe tasksCompleted

      repository.get(internalId, identifier, sessionId).futureValue.value mustBe tasksCompleted

      repository.reset(internalId, identifier, sessionId).futureValue.value mustBe defaultTask

      repository.get(internalId, identifier, sessionId).futureValue.value mustBe defaultTask
    }
  }

}