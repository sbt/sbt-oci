package com.lightbend.sbt.oci

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.{ Stager, universal }
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys.{ stage => _, stagingDirectory => _, _ }
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import scala.collection.immutable.{ Map, Seq }

object OciPlugin extends AutoPlugin {

  import Import._
  import OciKeys._
  import SbtNativePackager.autoImport._

  val autoImport = Import

  override def requires = universal.UniversalPlugin

  override def trigger = AllRequirements

  override def projectSettings =
    Seq(
      ociVersion := "0.6.0-dev",
      platform := Platform("darwin", "x64"),
      root := Root("rootfs", Some(true)),
      process := Process(
        terminal = Some(true),
        user = null,
        args = Seq("sh"),
        env = Some(Seq("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin", "TERM=xterm")),
        cwd = "/",
        capabilities = Some(Seq("CAP_AUDIT_WRITE", "CAP_KILL", "CAP_NET_BIND_SERVICE")),
        rlimits = Some(Seq(Rlimit("RLIMIT_NOFILE", 1024, 1024))),
        noNewPrivileges = Some(true)
      ),
      hostname := Some("runc"),
      mounts := Some(Seq(
        Mount("/proc", "proc", "proc", None),
        Mount("/dev", "tmpfs", "tmpfs", Some(Seq("nosuid", "strictatime", "mode=755", "size=65536k"))),
        Mount("/dev/pts", "devpts", "devpts", Some(Seq("nosuid", "noexec", "newinstance", "ptmxmode=0666", "mode=0620", "gid=5"))),
        Mount("/dev/shm", "tmpfs", "shm", Some(Seq("nosuid", "noexec", "nodev", "mode=1777", "size=65536k"))),
        Mount("/dev/mqueue", "mqueue", "mqueue", Some(Seq("nosuid", "noexec", "nodev"))),
        Mount("/sys", "sysfs", "sysfs", Some(Seq("nosuid", "noexec", "nodev", "ro"))),
        Mount("/sys/fs/cgroup", "cgroup", "cgroup", Some(Seq("nosuid", "noexec", "nodev", "relatime", "ro")))
      )),
      linux := Some(Linux(
        maskedPaths = Some(Seq(
          "/proc/kcore",
          "/proc/latency_stats",
          "/proc/timer_stats",
          "/proc/sched_debug"
        )),
        readonlyPaths = Some(Seq(
          "/proc/asound",
          "/proc/bus",
          "/proc/fs",
          "/proc/irq",
          "/proc/sys",
          "/proc/sysrq-trigger"
        )),
        resources = Some(Resources(devices = Seq(DeviceCgroup(allow = false, access = Some("rwm"))))),
        namespaces = Some(Seq(
          Namespace(NamespaceType.Pid),
          Namespace(NamespaceType.Network),
          Namespace(NamespaceType.Ipc),
          Namespace(NamespaceType.Uts),
          Namespace(NamespaceType.Mount)
        ))
      ))
    ) ++ ociSettings(Oci)

  private def ociSettings(config: Configuration): scala.collection.Seq[Def.Setting[_]] =
    inConfig(config)(Seq(
      NativePackagerKeys.executableScriptName := executableScriptName.value,
      NativePackagerKeys.packageName := packageName.value,
      NativePackagerKeys.stage <<= (streams, stagingDirectory, mappings) map Stager.stage(config.name),
      NativePackagerKeys.stagingDirectory := (target in config).value / "stage",
      target := target.value / "oci",

      NativePackagerKeys.defaultLinuxInstallLocation := "/opt/" + name.value
    ))

  private val ociVersion = settingKey[String]("OCI Specification version that the package types support.")
  private val platform = settingKey[Platform]("Platform is the host information for OS and Arch.")
  private val mounts = SettingKey[Option[Seq[Mount]]]("Mounts profile configuration for adding mounts to the container's filesystem.")
  private val hooks = SettingKey[Hooks]("Hooks are the commands run at various lifecycle events of the container.")
  private val annotations = SettingKey[Option[Map[String, String]]]("Annotations is an unstructured key value map that may be set by external tools to store and retrieve arbitrary metadata.")
  private val linux = SettingKey[Option[Linux]]("Linux is platform specific configuration for Linux based containers.")
  private val solaris = SettingKey[Option[Solaris]]("Solaris is platform specific configuration for Solaris containers.")
}