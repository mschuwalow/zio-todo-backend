addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.2.1")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"              % "0.9.13")
addSbtPlugin("com.github.gseitz" % "sbt-release"               % "1.0.11")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager"       % "1.3.21")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.2.11")

libraryDependencies += "com.spotify" % "docker-client" % "8.9.0"
