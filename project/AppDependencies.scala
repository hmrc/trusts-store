import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private lazy val hmrcMongoVersion     = "1.3.0"
  private lazy val bootstrapPlayVersion = "7.22.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.17",
    "org.scalatestplus"   %% "scalacheck-1-17"         % "3.2.17.0",
    "org.scalatestplus"   %% "mockito-4-11"            % "3.2.17.0",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % "test, it")
}
