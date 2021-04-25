import sbt._
import Keys._
import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._
import sbtrelease.{Git, Utilities}
import Utilities._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

object Release {
  val releaseBranch = "develop"
  val mergeBranch   = "master"

  val mergeReleaseVersion = ReleaseStep(action = st => {
    val git = st.extract.get(releaseVcs).get.asInstanceOf[Git]
    st.log.info(s"####### current branch: $releaseBranch")
    git.cmd("checkout", mergeBranch) ! st.log
    st.log.info(s"####### pull $mergeBranch")
    git.cmd("pull") ! st.log
    st.log.info(s"####### merge")
    git.cmd("merge", releaseBranch, "--no-ff", "--no-edit") ! st.log
    st.log.info(s"####### push")
    git.cmd("push", "origin", s"$mergeBranch:$mergeBranch") ! st.log
    st.log.info(s"####### checkout $releaseBranch")
    git.cmd("checkout", releaseBranch) ! st.log
    st
  })

  val releaseProcess: Seq[ReleaseStep] = Seq(
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    pushChanges,
    tagRelease,
    mergeReleaseVersion,
    ReleaseStep(releaseStepTask(publish in Docker)),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
}
