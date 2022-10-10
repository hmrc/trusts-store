import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "trusts-store"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*.Routes.*;prod.*;testOnlyDoNotUseInProd.*;testOnlyDoNotUseInAppConf.*;" +
      ".*BuildInfo.*;app.*;prod.*;config.*",
    ScoverageKeys.coverageMinimumStmtTotal := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, play.sbt.PlayScala, SbtDistributablesPlugin)
  .configs(IntegrationTest)
  .settings(
    inConfig(IntegrationTest)(itSettings),
    publishingSettings ++ scoverageSettings,
    resolvers += Resolver.jcenterRepo,
    RoutesKeys.routesImport += "models.flags.FeatureFlagName",
    PlayKeys.playDefaultPort := 9783,
    majorVersion := 0,
    scalaVersion := "2.12.16",
    SilencerSettings(),
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
  fork                         := false,
  javaOptions                  ++= Seq(
    "-Dconfig.resource=it.application.conf -Dlogback.configurationFile=logback-test.xml"
  )
)

addCommandAlias("scalastyleAll", "all scalastyle test:scalastyle it:scalastyle")
