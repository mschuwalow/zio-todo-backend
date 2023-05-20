addSbtPlugin("org.scalameta"    % "sbt-scalafmt"              % "2.5.0")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"              % "0.10.4")
addSbtPlugin("com.github.sbt"   % "sbt-release"               % "1.1.0")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager"       % "1.9.14")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.3.1")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
