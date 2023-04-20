import play.sbt.routes.RoutesKeys

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
    scoverageSettings,
    resolvers += Resolver.jcenterRepo,
    RoutesKeys.routesImport += "models.flags.FeatureFlagName",
    PlayKeys.playDefaultPort := 9783,
    majorVersion := 0,
    scalaVersion := "2.13.10",
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s"
    )
  )

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork := false,
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf -Dlogback.configurationFile=logback-test.xml"
  )
)

addCommandAlias("scalastyleAll", "all scalastyle test:scalastyle it:scalastyle")
addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
