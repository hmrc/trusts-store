import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrcMongo = "0.73.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"     % "5.24.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"            % hmrcMongo
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"         %% "play-test"                  % current,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"    % hmrcMongo,
    "uk.gov.hmrc"               %% "service-integration-test"   % "1.3.0-play-28",
    "org.scalatestplus.play"    %% "scalatestplus-play"         % "5.1.0",
    "com.vladsch.flexmark"      % "flexmark-all"                % "0.62.2",
    "org.scalatest"             %% "scalatest"                  % "3.2.14",
    "org.scalatestplus"         %% "mockito-4-6"                % "3.2.14.0",
    "org.scalatestplus"         %% "scalacheck-1-16"            % "3.2.14.0"
  ).map(_ % "test, it")

}
