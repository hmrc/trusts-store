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
import org.scalatest.{BeforeAndAfter, FreeSpec, MustMatchers}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.MimeTypes
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BodyParser, BodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.trustsstore.config.AppConfig
import uk.gov.hmrc.trustsstore.controllers.actions.{FakeIdentifierAction, IdentifierAction}

class SpecBase extends FreeSpec
  with MustMatchers
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfter
  with GuiceOneServerPerSuite
  with WireMockHelper {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  def application = applicationBuilder().build()

  def injector = application.injector

  def appConfig : AppConfig = injector.instanceOf[AppConfig]

  def fakeRequest = FakeRequest("POST", "")
    .withHeaders(CONTENT_TYPE -> MimeTypes.JSON)
    .withBody(Json.parse("{}"))

  def applicationBuilder(): GuiceApplicationBuilder = {
    new GuiceApplicationBuilder()
      .configure(
        Seq(
          "metrics.enabled" -> false,
          "auditing.enabled" -> false
        ): _*
      )
    .overrides(
      bind[IdentifierAction].toInstance(new FakeIdentifierAction())
    )
  }

}






