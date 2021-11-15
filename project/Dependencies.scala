import sbt._

object Dependencies {

  object Versions {
    val betterMonadicFor = "0.3.1"
    val circe            = "0.14.1"
    val doobie           = "0.13.4"
    val flyway           = "8.0.4"
    val h2               = "1.4.200"
    val http4s           = "0.21.26"
    val jawn             = "1.0.1"
    val kindProjector    = "0.13.2"
    val log4j            = "2.14.1"
    val organizeImports  = "0.6.0"
    val pureConfig       = "0.17.0"
    val zio              = "1.0.12"
    val zioInteropCats   = "2.5.1.0"
    val zioLogging       = "0.5.13"
  }
  import Versions._

  val App =
    List(
      "com.github.pureconfig"        %% "pureconfig"          % pureConfig,
      "com.h2database"                % "h2"                  % h2,
      "dev.zio"                      %% "zio-interop-cats"    % zioInteropCats,
      "dev.zio"                      %% "zio-logging-slf4j"   % zioLogging,
      "dev.zio"                      %% "zio-logging"         % zioLogging,
      "dev.zio"                      %% "zio-test-sbt"        % zio   % "test",
      "dev.zio"                      %% "zio-test"            % zio   % "test",
      "dev.zio"                      %% "zio"                 % zio,
      "io.circe"                     %% "circe-core"          % circe,
      "io.circe"                     %% "circe-generic"       % circe,
      "io.circe"                     %% "circe-literal"       % circe % "test",
      "org.apache.logging.log4j"      % "log4j-api"           % log4j,
      "org.apache.logging.log4j"      % "log4j-core"          % log4j,
      "org.apache.logging.log4j"      % "log4j-slf4j-impl"    % log4j,
      "org.flywaydb"                  % "flyway-core"         % flyway,
      "org.http4s"                   %% "http4s-blaze-server" % http4s,
      "org.http4s"                   %% "http4s-circe"        % http4s,
      "org.http4s"                   %% "http4s-dsl"          % http4s,
      "org.tpolecat"                 %% "doobie-core"         % doobie,
      "org.tpolecat"                 %% "doobie-h2"           % doobie,
      "org.tpolecat"                 %% "doobie-hikari"       % doobie,
      "org.typelevel"                %% "jawn-parser"         % jawn  % "test",
      compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicFor),
      compilerPlugin(("org.typelevel" % "kind-projector"      % kindProjector).cross(CrossVersion.full))
    )

  val ScalaFix =
    List(
      "com.github.liancheng" %% "organize-imports" % organizeImports
    )

}
