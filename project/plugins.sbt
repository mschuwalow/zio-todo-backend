addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.4.2")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"              % "0.9.21")
addSbtPlugin("com.github.gseitz" % "sbt-release"               % "1.0.13")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager"       % "1.7.5")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.2.13")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
