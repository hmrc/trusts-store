/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.trustsstore

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}
import play.api.Application
import play.api.http.MimeTypes
import play.api.inject.{Injector, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, BodyParser, BodyParsers, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.trustsstore.config.AppConfig
import uk.gov.hmrc.trustsstore.controllers.actions.{FakeIdentifierAction, IdentifierAction}

class BaseSpec extends FreeSpec
  with GuiceOneAppPerSuite
  with MustMatchers
  with MockitoSugar
  with OptionValues
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfter
 {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def injector: Injector = app.injector

  def appConfig: AppConfig = injector.instanceOf[AppConfig]

  def fakeUtr = "1234567890"

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





