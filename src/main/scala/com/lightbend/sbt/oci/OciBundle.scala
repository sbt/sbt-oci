package com.lightbend.sbt.oci

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.universal.Archives
import sbt._
import sbt.Keys._
import SbtNativePackager.Universal

object OciBundle extends AutoPlugin {

  import Import._
  import SbtNativePackager.autoImport._

  val autoImport = Import

  override def requires = SbtNativePackager

  override def trigger = AllRequirements

  override def projectSettings =
    Seq.empty
}
