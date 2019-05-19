val FS2Version        = "1.0.4"
val Http4sVersion     = "0.20.1"
val CirceVersion      = "0.12.0-M1"
val Specs2Version     = "4.1.0"
val LogbackVersion    = "1.2.3"
val ScalaLogVersion   = "3.9.2"
val PureConfigVersion = "0.10.2"
val ZioVersion        = "1.0-RC4"
val ScalaTestVersion  = "3.0.5"
val DoobieVersion     = "0.7.0-M5"
val H2Version         = "1.4.199"
val FlywayVersion     = "5.2.4"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    packageName in Docker := "zio-todo",
    dockerUsername in Docker := Some("grumpyraven"),
    dockerExposedPorts in Docker := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq("MIT" -> url(s"https://github.com/sbt/${name.value}/blob/${version.value}/LICENSE")),
    scalaVersion := "2.12.8",
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-Xfuture",
      "-encoding", "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Ypartial-unification",
      "-Xfatal-warnings",
      "-Xlint:_",
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
      "co.fs2"                      %% "fs2-core"                 % FS2Version,
      "org.http4s"                  %% "http4s-blaze-server"      % Http4sVersion,
      "org.http4s"                  %% "http4s-blaze-client"      % Http4sVersion,
      "org.http4s"                  %% "http4s-circe"             % Http4sVersion,
      "org.http4s"                  %% "http4s-dsl"               % Http4sVersion,
      "io.circe"                    %% "circe-generic"            % CirceVersion,

      "org.tpolecat"                %% "doobie-core"              % DoobieVersion,
      "org.tpolecat"                %% "doobie-h2"                % DoobieVersion,
      "org.tpolecat"                %% "doobie-hikari"            % DoobieVersion,

      "com.h2database"              %  "h2"                       % H2Version,
      "org.flywaydb"                %  "flyway-core"              % FlywayVersion,

      "com.typesafe.scala-logging"  %% "scala-logging"            % ScalaLogVersion,

      "com.github.pureconfig"       %% "pureconfig"               % PureConfigVersion,
      "com.github.pureconfig"       %% "pureconfig-cats-effect"   % PureConfigVersion,

      "org.scalaz"                  %% "scalaz-zio"               % ZioVersion,
      "org.scalaz"                  %% "scalaz-zio-interop-cats"  % ZioVersion,

      "org.scalatest"               %% "scalatest"                % ScalaTestVersion % "test",

      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")
    )
  )
