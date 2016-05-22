package com.lightbend.sbt.oci

import java.io.File
import sbt._
import sbt.Keys.{
  name,
  target,
  mappings,
  sourceDirectory,
  streams,
  packageBin
}
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.{ Stager, MappingsHelper }
import com.typesafe.sbt.packager.linux.LinuxPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin

object OciPlugin extends AutoPlugin {

  object autoImport extends Import {
    val Oci = config("oci")
  }

  import autoImport._
  import OciKeys._
  import UniversalPlugin.autoImport._
  import LinuxPlugin.autoImport.defaultLinuxInstallLocation

  override def requires = UniversalPlugin

  override def trigger = AllRequirements

  override def projectConfigurations: Seq[Configuration] =
    Seq(Oci)

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
    ) ++ mapGenericFilesToOci ++ ociSettings

  private def ociSettings: scala.collection.Seq[Def.Setting[_]] =
    inConfig(Oci)(Seq(
      executableScriptName := executableScriptName.value,
      mappings ++= ociPackageMappings.value,
      name := name.value,
      packageName := packageName.value,
      sourceDirectory := sourceDirectory.value / "oci",
      target := target.value / "oci",
      stagingDirectory := (target in Oci).value / "stage",
      stage <<= (streams, stagingDirectory, mappings) map Stager.stage(Oci.name),

      packageBin <<= packageBin in Oci,
      dist <<= dist in Oci,
      defaultLinuxInstallLocation := s"/opt/${name.value}",

      ociPackageMappings <<= sourceDirectory map { dir =>
        println("asdads")
        MappingsHelper.contentOf(dir)
      }
    ))

  private def mapGenericFilesToOci: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) = {
      for {
        (f, path) <- from
        newPath = "%s/%s" format (dest, path)
      } yield (f, newPath)
    }

    inConfig(Oci)(Seq(
      mappings <<= (mappings in Universal, defaultLinuxInstallLocation) map { (mappings, dest) =>
        renameDests(mappings, dest)
      }
    ))
  }

  private val ociPackageMappings = taskKey[Seq[(File, String)]]("Generates location mappings for OCI.")
  private val ociTarget = taskKey[String]("Defines target used when building OCI container")

  private val ociVersion = settingKey[String]("OCI Specification version that the package types support.")
  private val platform = settingKey[Platform]("Platform is the host information for OS and Arch.")
  private val mounts = SettingKey[Option[Seq[Mount]]]("Mounts profile configuration for adding mounts to the container's filesystem.")
  private val hooks = SettingKey[Hooks]("Hooks are the commands run at various lifecycle events of the container.")
  private val annotations = SettingKey[Option[Map[String, String]]]("Annotations is an unstructured key value map that may be set by external tools to store and retrieve arbitrary metadata.")
  private val linux = SettingKey[Option[Linux]]("Linux is platform specific configuration for Linux based containers.")
  private val solaris = SettingKey[Option[Solaris]]("Solaris is platform specific configuration for Solaris containers.")
}