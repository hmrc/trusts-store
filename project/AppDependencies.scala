import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private lazy val hmrcMongoVersion     = "2.14.0"
  private lazy val bootstrapPlayVersion = "10.8.0"

  private val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus" %% "scalacheck-1-20"         % "3.2.20.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
