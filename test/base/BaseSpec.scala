/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.MimeTypes
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier

class BaseSpec extends FreeSpec
  with GuiceOneAppPerSuite
  with MustMatchers
  with MockitoSugar
  with OptionValues
  with EitherValues
  with ScalaFutures
  with Inside
  with BeforeAndAfterEach
  with BeforeAndAfter {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def fakeUtr: String = "1234567890"

  def fakeInternalId: String = "Int-328969d0-557e-4559-96ba-074d0597107e"

  def fakeRequest: FakeRequest[JsValue] = FakeRequest("POST", "")
    .withHeaders(CONTENT_TYPE -> MimeTypes.JSON)
    .withBody(Json.parse("{}"))

  def injectedParsers: PlayBodyParsers = injector.instanceOf[PlayBodyParsers]

  protected def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        Seq(
          "metrics.enabled" -> false,
          "auditing.enabled" -> false
        ): _*
      )
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(injectedParsers))
      )
  }

}






