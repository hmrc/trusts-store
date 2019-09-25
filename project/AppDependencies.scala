import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc"             %% "play-reactivemongo"       % "6.2.0",
    "uk.gov.hmrc"             %% "bootstrap-play-25"        % "3.10.0"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.17.0"                % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.1"                 % "test, it",
    "org.mockito"             %  "mockito-core"             % "1.10.19"               % "test, it"
  )

}
