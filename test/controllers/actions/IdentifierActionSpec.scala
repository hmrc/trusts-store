/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import akka.stream.Materializer
import base.BaseSpec
import com.google.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{Action, BodyParsers, PlayBodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends BaseSpec {

  implicit lazy val mtrlzr: Materializer = injector.instanceOf[Materializer]

  val playBodyParsers: PlayBodyParsers = injector.instanceOf[PlayBodyParsers]

  class Harness(authAction: IdentifierAction) {
    def onSubmit(): Action[JsValue] = authAction.apply(playBodyParsers.json)(_ => Results.Ok)
  }

  def bodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]

  private def authRetrievals(affinityGroup: AffinityGroup) =
    Future.successful(new ~(Some("id"), Some(affinityGroup)))

  private def insufficientAuthRetrievals =
    Future.successful(new ~(None, None))

  private val agentAffinityGroup = AffinityGroup.Agent
  private val orgAffinityGroup   = AffinityGroup.Organisation

  "Auth Action must" when {

    "when Agent user" should {

      "allow user to continue" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeAuthConnector(authRetrievals(agentAffinityGroup)), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "when Organisation user" should {

      "allow user to continue" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeAuthConnector(authRetrievals(orgAffinityGroup)), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe OK
      }

    }

    "when Individual user" should {

      "be returned an unauthorized response" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeAuthConnector(authRetrievals(Individual)), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }

    }

    "the user hasn't logged in" should {

      "be returned an unauthorized response" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "the user's session has expired" should {

      "be returned an unauthorized response" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "handle insufficient retrievals" should {

      "by returning an unauthorized response" in {
        val authAction =
          new AuthenticatedIdentifierAction(new FakeAuthConnector(insufficientAuthRetrievals), bodyParsers)
        val controller = new Harness(authAction)
        val result     = controller.onSubmit()(fakeRequest)

        status(result) mustBe UNAUTHORIZED
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}

class FakeAuthConnector(stubbedRetrievalResult: Future[_]) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    stubbedRetrievalResult.map(_.asInstanceOf[A])(ec)
}
