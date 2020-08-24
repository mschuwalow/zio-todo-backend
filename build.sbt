addCommandAlias("build", "prepare; test")
addCommandAlias("prepare", "fix; fmt")
addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "compile:scalafix --check; test:scalafix --check"
)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "fmtCheck",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.5.4"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    packageName in Docker := "zio-todo",
    dockerUsername in Docker := Some("grumpyraven"),
    dockerExposedPorts in Docker := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq(
      "MIT" -> url(
        s"https://github.com/mschuwalow/${name.value}/blob/v${version.value}/LICENSE"
      )
    ),
    scalaVersion := "2.13.1",
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Xfatal-warnings",
      "-Xlint:-infer-any,_",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:_"
    ) ++ (if (isSnapshot.value) Seq.empty
          else
            Seq(
              "-opt:l:inline"
            )),
    libraryDependencies ++= Seq(
      "org.http4s"               %% "http4s-blaze-server" % "0.21.7",
      "org.http4s"               %% "http4s-circe"        % "0.21.7",
      "org.http4s"               %% "http4s-dsl"          % "0.21.7",
      "io.circe"                 %% "circe-core"          % "0.13.0",
      "io.circe"                 %% "circe-generic"       % "0.13.0",
      "io.circe"                 %% "circe-literal"       % "0.13.0" % "test",
      "org.tpolecat"             %% "doobie-core"         % "0.8.8",
      "org.tpolecat"             %% "doobie-h2"           % "0.8.8",
      "org.tpolecat"             %% "doobie-hikari"       % "0.8.8",
      "org.typelevel"            %% "jawn-parser"         % "1.0.0" % "test",
      "dev.zio"                  %% "zio"                 % "1.0.1",
      "dev.zio"                  %% "zio-test"            % "1.0.1" % "test",
      "dev.zio"                  %% "zio-test-sbt"        % "1.0.1" % "test",
      "dev.zio"                  %% "zio-interop-cats"    % "2.1.4.0",
      "dev.zio"                  %% "zio-logging"         % "0.2.9",
      "dev.zio"                  %% "zio-logging-slf4j"   % "0.2.9",
      "org.flywaydb"             % "flyway-core"          % "6.5.5",
      "com.h2database"           % "h2"                   % "1.4.200",
      "org.apache.logging.log4j" % "log4j-api"            % "2.13.3",
      "org.apache.logging.log4j" % "log4j-core"           % "2.13.3",
      "org.apache.logging.log4j" % "log4j-slf4j-impl"     % "2.13.3",
      "com.github.pureconfig"    %% "pureconfig"          % "0.12.3",
      "com.lihaoyi"              %% "sourcecode"          % "0.1.9",
      ("com.github.ghik" % "silencer-lib" % "1.6.0" % "provided")
        .cross(CrossVersion.full),
      // plugins
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      compilerPlugin(
        ("org.typelevel" % "kind-projector" % "0.11.0").cross(CrossVersion.full)
      ),
      compilerPlugin(
        ("com.github.ghik" % "silencer-plugin" % "1.6.0")
          .cross(CrossVersion.full)
      ),
      compilerPlugin(scalafixSemanticdb)
    )
  )

//release
{
  import ReleaseTransformations._
  import ReleasePlugin.autoImport._
  import sbtrelease.{ Git, Utilities }
  import Utilities._

  val releaseBranch = "develop"
  val mergeBranch   = "master"

  val mergeReleaseVersion = ReleaseStep(action = st => {
    val git = st.extract.get(releaseVcs).get.asInstanceOf[Git]
    st.log.info(s"####### current branch: $releaseBranch")
    git.cmd("checkout", mergeBranch) ! st.log
    st.log.info(s"####### pull $mergeBranch")
    git.cmd("pull") ! st.log
    st.log.info(s"####### merge")
    git.cmd("merge", releaseBranch, "--no-ff", "--no-edit") ! st.log
    st.log.info(s"####### push")
    git.cmd("push", "origin", s"$mergeBranch:$mergeBranch") ! st.log
    st.log.info(s"####### checkout $releaseBranch")
    git.cmd("checkout", releaseBranch) ! st.log
    st
  })

  releaseProcess := Seq(
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    pushChanges,
    tagRelease,
    mergeReleaseVersion,
    ReleaseStep(releaseStepTask(publish in Docker)),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
}
