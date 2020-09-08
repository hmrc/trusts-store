import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "trusts-store"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;app.Routes.*;prod.*;testOnlyDoNotUseInProd.*;testOnlyDoNotUseInAppConf.*;" +
      "uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*;.*ClaimedTrustsRepository;.*AppConfig",
    ScoverageKeys.coverageMinimum := 65,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(publishingSettings ++ scoverageSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(RoutesKeys.routesImport += "uk.gov.hmrc.trustsstore.models.FeatureFlagName")
  .settings(
    PlayKeys.playDefaultPort := 9783,
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )

 lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories   := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution            := false,
  fork                         := true,
  javaOptions                  ++= Seq(
    "-Dconfig.resource=it.application.conf"
  )
)

dependencyOverrides ++= AppDependencies.overrides