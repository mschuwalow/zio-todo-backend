import sbt._

object Dependencies {

  object Versions {
    val circe      = "0.13.0"
    val doobie     = "0.12.1"
    val http4s     = "0.21.20"
    val log4j      = "2.14.1"
    val zio        = "1.0.5"
    val zioLogging = "0.5.7"
  }
  import Versions._

  val App =
    List(
      "org.http4s"                   %% "http4s-blaze-server" % http4s,
      "org.http4s"                   %% "http4s-circe"        % http4s,
      "org.http4s"                   %% "http4s-dsl"          % http4s,
      "io.circe"                     %% "circe-core"          % circe,
      "io.circe"                     %% "circe-generic"       % circe,
      "io.circe"                     %% "circe-literal"       % circe   % "test",
      "org.tpolecat"                 %% "doobie-core"         % doobie,
      "org.tpolecat"                 %% "doobie-h2"           % doobie,
      "org.tpolecat"                 %% "doobie-hikari"       % doobie,
      "org.typelevel"                %% "jawn-parser"         % "1.0.1" % "test",
      "dev.zio"                      %% "zio"                 % zio,
      "dev.zio"                      %% "zio-test"            % zio     % "test",
      "dev.zio"                      %% "zio-test-sbt"        % zio     % "test",
      "dev.zio"                      %% "zio-interop-cats"    % "2.3.1.0",
      "dev.zio"                      %% "zio-logging"         % zioLogging,
      "dev.zio"                      %% "zio-logging-slf4j"   % zioLogging,
      "org.flywaydb"                  % "flyway-core"         % "7.7.0",
      "com.h2database"                % "h2"                  % "1.4.200",
      "org.apache.logging.log4j"      % "log4j-api"           % log4j,
      "org.apache.logging.log4j"      % "log4j-core"          % log4j,
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"    % log4j,
      "com.github.pureconfig"        %% "pureconfig"          % "0.14.1",
      "com.lihaoyi"                  %% "sourcecode"          % "0.2.4",
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      compilerPlugin(("org.typelevel" % "kind-projector"      % "0.11.3").cross(CrossVersion.full))
    )
}
