addCommandAlias("build", "prepare; test")
addCommandAlias("prepare", "fix; fmt")
addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fixCheck", "compile:scalafix --check; test:scalafix --check")
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("fmtCheck", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

inThisBuild(
  List(
    organization      := "com.schuwalow",
    developers        := List(
      Developer(
        "mschuwalow",
        "Maxim Schuwalow",
        "maxim.schuwalow@gmail.com",
        url("https://github.com/mschuwalow")
      )
    ),
    licenses          := Seq(
      "MIT" -> url(s"https://github.com/mschuwalow/zio-todo-backend/blob/master/LICENSE")
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalaVersion      := "2.13.14"
  )
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name                        := "zio-todo-backend",
    dockerBaseImage             := "eclipse-temurin:17-jre",
    libraryDependencies ++= Dependencies.App,
    scalacOptions               := Options.scalacOptions(scalaVersion.value, isSnapshot.value),
    testFrameworks              := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    publish / skip              := true,
    Compile / mainClass         := Some("com.schuwalow.todo.Main"),
    Docker / dockerExposedPorts := Seq(8080),
    Docker / dockerUsername     := Some("mschuwalow"),
    inConfig(Docker)(DynVerPlugin.buildSettings ++ Seq(dynverSeparator := "-"))
  )
