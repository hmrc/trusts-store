/*
 * Copyright 2024 HM Revenue & Customs
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

package base

import config.AppConfig
import controllers.actions.{FakeIdentifierAction, IdentifierAction}
import generators.ModelGenerators
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.MimeTypes
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier

class BaseSpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with Matchers
    with MockitoSugar
    with OptionValues
    with EitherValues
    with ScalaFutures
    with Inside
    with BeforeAndAfterEach
    with BeforeAndAfter
    with ScalaCheckPropertyChecks
    with ModelGenerators {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def fakeUtr: String = "1234567890"

  def fakeInternalId: String = "Int-328969d0-557e-4559-96ba-074d0597107e"

  def fakeSessionId: String = "session-d41ebbc3-38bc-4276-86da-5533eb878e37"

  def fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "")
    .withHeaders(CONTENT_TYPE -> MimeTypes.JSON)
    .withBody(Json.parse("{}"))

  def injectedParsers: PlayBodyParsers = injector.instanceOf[PlayBodyParsers]

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(injectedParsers))
      )
      .configure(
        "auditing.enabled" -> false,
        "metrics.enabled"  -> false
      )

}
