import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "0.46.0",
    "org.reactivemongo"       %% "play2-reactivemongo"      % "0.18.5-play26"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.24.1"                % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it",
    "org.mockito"             %  "mockito-core"             % "1.10.19"               % "test, it"
  )

}
