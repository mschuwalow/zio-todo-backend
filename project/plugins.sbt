addSbtPlugin("org.scalameta"    % "sbt-scalafmt"              % "2.4.6")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"              % "0.10.4")
addSbtPlugin("com.github.sbt"   % "sbt-release"               % "1.1.0")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager"       % "1.9.11")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
