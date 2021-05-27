import sbt._
import Keys._

object Options {

  def scalacOptions(scalaVersion: String, optimize: Boolean) = {
    val baseOptions = Seq(
      "-feature",
      "-deprecation",
//      "-explaintypes",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Ykind-projector"
//      "-Xfatal-warnings"
//      "-Xlint:-byname-implicit,_",
//      "-Ywarn-value-discard",
//      "-Ywarn-numeric-widen",
//      "-Ywarn-extra-implicit",
//      "-Ywarn-unused"
    )

    val optimizeOptions = Seq.empty
//      if (optimize) {
//        Seq(
////          "-opt:l:inline"
//        )
//      } else Seq.empty

    baseOptions ++ optimizeOptions
//    baseOptions
  }
}
