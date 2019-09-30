package uk.gov.hmrc.trustsstore.repositories

import org.scalatest.concurrent.IntegrationPatience
import uk.gov.hmrc.trustsstore.BaseSpec
import uk.gov.hmrc.trustsstore.suite.MongoSuite
import play.api.test.Helpers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class ClaimedTrustsRepositorySpec extends BaseSpec with IntegrationPatience with MongoSuite {

  "a claimed trusts repository" - {
    val internalId = "Int-328969d0-557e-4559-96ba-074d0597107e"

    "must be able to retrieve trusts claims" ignore {
      database.flatMap(_.drop()).futureValue

      val application = applicationBuilder().build()

      running(application)
    }
  }
}
