import sbt._

object Dependencies {

  object Versions {
    val betterMonadicFor = "0.3.1"
    val circe            = "0.14.14"
    val doobie           = "1.0.0-RC10"
    val flyway           = "11.13.1"
    val h2               = "2.3.232"
    val http4s           = "0.23.31"
    val blaze            = "0.23.17"
    val kindProjector    = "0.13.3"
    val log4j            = "2.25.2"
    val zio              = "2.1.21"
    val zioConfig        = "4.0.5"
    val zioInteropCats   = "23.1.0.5"
    val zioLogging       = "2.5.1"
  }
  import Versions._

  val App =
    List(
      "com.h2database"           % "h2"                  % h2,
      "dev.zio"                 %% "zio-config-magnolia" % zioConfig,
      "dev.zio"                 %% "zio-config-typesafe" % zioConfig,
      "dev.zio"                 %% "zio-interop-cats"    % zioInteropCats,
      "dev.zio"                 %% "zio-logging-slf4j"   % zioLogging,
      "dev.zio"                 %% "zio-logging"         % zioLogging,
      "dev.zio"                 %% "zio-test-sbt"        % zio   % "test",
      "dev.zio"                 %% "zio-test"            % zio   % "test",
      "dev.zio"                 %% "zio"                 % zio,
      "io.circe"                %% "circe-core"          % circe,
      "io.circe"                %% "circe-generic"       % circe,
      "io.circe"                %% "circe-literal"       % circe % "test",
      "org.apache.logging.log4j" % "log4j-api"           % log4j,
      "org.apache.logging.log4j" % "log4j-core"          % log4j,
      "org.apache.logging.log4j" % "log4j-slf4j-impl"    % log4j,
      "org.flywaydb"             % "flyway-core"         % flyway,
      "org.http4s"              %% "http4s-blaze-server" % blaze,
      "org.http4s"              %% "http4s-circe"        % http4s,
      "org.http4s"              %% "http4s-dsl"          % http4s,
      "org.tpolecat"            %% "doobie-core"         % doobie,
      "org.tpolecat"            %% "doobie-h2"           % doobie,
      "org.tpolecat"            %% "doobie-hikari"       % doobie,
      compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicFor),
      compilerPlugin(("org.typelevel" % "kind-projector" % kindProjector).cross(CrossVersion.full))
    )

}
