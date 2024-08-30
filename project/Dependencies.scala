import sbt._

object Dependencies {

  object Versions {
    val betterMonadicFor = "0.3.1"
    val circe            = "0.14.9"
    val doobie           = "1.0.0-RC5"
    val flyway           = "10.17.2"
    val h2               = "2.3.232"
    val http4s           = "0.23.27"
    val blaze            = "0.23.16"
    val kindProjector    = "0.13.3"
    val log4j            = "2.23.1"
    val zio              = "2.1.9"
    val zioConfig        = "4.0.2"
    val zioInteropCats   = "23.1.0.3"
    val zioLogging       = "2.3.1"
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
