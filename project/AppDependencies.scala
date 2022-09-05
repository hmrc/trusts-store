import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"     % "5.24.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"            % "0.70.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"         %% "play-test"              % current                 % "test",
    "uk.gov.hmrc.mongo"        %% "hmrc-mongo-test-play-28" % "0.70.0"                % "test",
    "org.scalatest"           %% "scalatest"                % "3.0.9"                 % "test",
    "org.scalatestplus"       %% "mockito-3-4"              % "3.3.0.0-SNAP3"         % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "1.1.0-play-28"         % "test, it",
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.2"                % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % "test, it",
    "org.scalatestplus"       %% "scalacheck-1-15"          % "3.2.9.0"               % "test",
    "com.vladsch.flexmark"    % "flexmark-all"              % "0.35.10"               % "test, it",
    "org.mockito"             %  "mockito-core"             % "1.10.19"               % "test, it"
  )

  val akkaVersion = "2.6.15"
  val akkaHttpVersion = "10.1.12"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec"     %  "commons-codec" % "1.12"
  )

}
