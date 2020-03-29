val Http4sVersion   = "0.21.0-M5"
val CirceVersion    = "0.12.3"
val DoobieVersion   = "0.8.6"
val ZIOVersion      = "1.0.0-RC18-2"
val SilencerVersion = "1.4.4"
val Log4j2Version   = "2.13.1"

addCommandAlias("build", "prepare; testJVM")
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

scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.3.1"

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
      "org.http4s"               %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"               %% "http4s-circe"        % Http4sVersion,
      "org.http4s"               %% "http4s-dsl"          % Http4sVersion,
      "io.circe"                 %% "circe-core"          % CirceVersion,
      "io.circe"                 %% "circe-generic"       % CirceVersion,
      "io.circe"                 %% "circe-literal"       % CirceVersion % "test",
      "org.tpolecat"             %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"             %% "doobie-h2"           % DoobieVersion,
      "org.tpolecat"             %% "doobie-hikari"       % DoobieVersion,
      "dev.zio"                  %% "zio"                 % ZIOVersion,
      "dev.zio"                  %% "zio-test"            % ZIOVersion % "test",
      "dev.zio"                  %% "zio-test-sbt"        % ZIOVersion % "test",
      "dev.zio"                  %% "zio-interop-cats"    % "2.0.0.0-RC12",
      "org.flywaydb"             % "flyway-core"          % "5.2.4",
      "com.h2database"           % "h2"                   % "1.4.199",
      "org.apache.logging.log4j" % "log4j-api"            % Log4j2Version,
      "org.apache.logging.log4j" % "log4j-core"           % Log4j2Version,
      "org.apache.logging.log4j" % "log4j-slf4j-impl"     % Log4j2Version,
      "com.github.pureconfig"    %% "pureconfig"          % "0.12.1",
      "com.lihaoyi"              %% "sourcecode"          % "0.1.7",
      ("com.github.ghik" % "silencer-lib" % SilencerVersion % "provided")
        .cross(CrossVersion.full),
      // plugins
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      compilerPlugin(
        ("org.typelevel" % "kind-projector" % "0.11.0").cross(CrossVersion.full)
      ),
      compilerPlugin(
        ("com.github.ghik" % "silencer-plugin" % SilencerVersion)
          .cross(CrossVersion.full)
      ),
      compilerPlugin(scalafixSemanticdb)
    )
  )

//release
import ReleaseTransformations._
import ReleasePlugin.autoImport._
import sbtrelease.{ Git, Utilities }
import Utilities._

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

val mergeBranch = "master"

val mergeReleaseVersion = ReleaseStep(action = st => {
  val git       = st.extract.get(releaseVcs).get.asInstanceOf[Git]
  val curBranch = (git.cmd("rev-parse", "--abbrev-ref", "HEAD") !!).trim
  st.log.info(s"####### current branch: $curBranch")
  git.cmd("checkout", mergeBranch) ! st.log
  st.log.info(s"####### pull $mergeBranch")
  git.cmd("pull") ! st.log
  st.log.info(s"####### merge")
  git.cmd("merge", curBranch, "--no-ff", "--no-edit") ! st.log
  st.log.info(s"####### push")
  git.cmd("push", "origin", s"$mergeBranch:$mergeBranch") ! st.log
  st.log.info(s"####### checkout $curBranch")
  git.cmd("checkout", curBranch) ! st.log
  st
})
