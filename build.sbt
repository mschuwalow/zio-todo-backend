val Http4sVersion = "0.20.1"
val CirceVersion  = "0.12.0-M1"
val DoobieVersion = "0.7.0-M5"

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    packageName in Docker := "zio-todo",
    dockerUsername in Docker := Some("grumpyraven"),
    dockerExposedPorts in Docker := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq("MIT" -> url(s"https://github.com/mschuwalow/${name.value}/blob/v${version.value}/LICENSE")),
    scalaVersion := "2.12.8",
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-Xfuture",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Ypartial-unification",
      "-Xfatal-warnings",
      "-Xlint:-infer-any,_",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:_",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-opt:l:inline"
    ),
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "io.circe"              %% "circe-core"          % CirceVersion,
      "io.circe"              %% "circe-generic"       % CirceVersion,
      "io.circe"              %% "circe-literal"       % CirceVersion % "test",
      "org.tpolecat"          %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"           % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % DoobieVersion,
      "org.flywaydb"          % "flyway-core"          % "5.2.4",
      "com.h2database"        % "h2"                   % "1.4.199",
      "org.slf4j"             % "slf4j-log4j12"        % "1.7.26",
      "com.github.pureconfig" %% "pureconfig"          % "0.10.2",
      "dev.zio"               %% "zio"                 % "1.0.0-RC9-4",
      "dev.zio"               %% "zio-interop-cats"    % "1.3.1.0-RC3",
      "dev.zio"               %% "zio-delegate"        % "0.0.3",
      "com.lihaoyi"           %% "sourcecode"          % "0.1.7",
      "org.scalatest"         %% "scalatest"           % "3.0.5" % "test",
      compilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.4"),
      compilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.0-M4"),
      compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full))
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
