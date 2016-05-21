package com.lightbend.sbt.oci

import sbt.SettingKey
import scala.concurrent.duration.Duration
import scala.collection.immutable.{ Seq, Map }

object Import {
  object OciKeys {

    val ociVersion = SettingKey[String](
      "oci-version",
      "OCI Specification version that the package types support."
    )

    val platform = SettingKey[Platform](
      "oci-platform",
      "Platform is the host information for OS and Arch."
    )

    val root = SettingKey[Root](
      "oci-root",
      "Root information for the container's filesystem."
    )

    val process = SettingKey[Process](
      "oci-process",
      "Process is the container's main process."
    )

    val hostname = SettingKey[Option[String]](
      "oci-hostname",
      "Hostname is the container's host name."
    )

    val mounts = SettingKey[Option[Seq[Mount]]](
      "oci-mounts",
      "Mounts profile configuration for adding mounts to the container's filesystem."
    )

    val hooks = SettingKey[Hooks](
      "oci-hooks",
      "Hooks are the commands run at various lifecycle events of the container."
    )

    val annotations = SettingKey[Option[Map[String, String]]](
      "oci-annotations",
      "Annotations is an unstructured key value map that may be set by external tools to store and retrieve arbitrary metadata."
    )

    val linux = SettingKey[Option[Linux]](
      "oci-linux",
      "Linux is platform specific configuration for Linux based containers."
    )

    val solaris = SettingKey[Option[Solaris]](
      "oci-Solaris",
      "Solaris is platform specific configuration for Solaris containers."
    )
  }

  case class Platform(os: String, arch: String)

  case class Root(path: String, readonly: Option[Boolean] = None)

  case class Process(
    terminal: Option[Boolean] = None,
    user: User,
    args: Seq[String] = Seq.empty,
    env: Option[Seq[String]] = None,
    cwd: String,
    capabilities: Option[Seq[String]] = None,
    rlimits: Option[Seq[Rlimit]] = None,
    noNewPrivileges: Option[Boolean] = None,
    apparmorProfile: Option[String] = None,
    selinuxLabel: Option[String] = None)

  case class User(uid: Int, gid: Int, additionalGids: Seq[Int] = Seq.empty)

  case class Rlimit(`type`: String, hard: Long, soft: Long)

  case class Mount(destination: String, `type`: String, source: String, options: Option[Seq[String]] = None)

  case class Hooks(prestart: Option[Seq[Hook]] = None, poststart: Option[Seq[Hook]] = None, poststop: Option[Seq[Hook]] = None)

  case class Hook(path: String, args: Seq[String] = Seq.empty, env: Seq[String] = Seq.empty, timeout: Duration)

  case class Linux(
    uidMappings: Option[Seq[IdMapping]] = None,
    gidMappings: Option[Seq[IdMapping]] = None,
    sysctl: Option[Map[String, String]] = None,
    resources: Option[Resources] = None,
    cgroupsPath: Option[String] = None,
    namespaces: Option[Seq[Namespace]] = None,
    devices: Option[Seq[Device]] = None,
    seccomp: Option[Seccomp] = None,
    rootfsPropagation: Option[String] = None,
    maskedPaths: Option[Seq[String]] = None,
    readonlyPaths: Option[Seq[String]] = None,
    mountLabel: Option[String] = None)

  case class IdMapping(hostID: Int, containerID: Int, size: Int)

  case class Resources()

  case class Namespace(`type`: NamespaceType, path: Option[String] = None)

  sealed trait NamespaceType {
    override def toString = this.getClass.toString.toLowerCase
  }

  object NamespaceType {
    case object Pid extends NamespaceType
    case object Network extends NamespaceType
    case object Mount extends NamespaceType
    case object Ipc extends NamespaceType
    case object Uts extends NamespaceType
    case object User extends NamespaceType
  }

  case class Device(
    path: String,
    `type`: String,
    major: Long,
    minor: Long,
    fileMode: Option[Int] = None, // TODO: Create a `FileMode` adt. Reference: https://golang.org/pkg/os/#FileMode
    uid: Option[Int] = None,
    gid: Option[Int] = None)

  // TODO: Add Seccomp parameters
  case class Seccomp()

  case class Solaris(
    milestone: Option[String] = None,
    limitpriv: Option[String] = None,
    maxShmMemory: Option[String] = None,
    anet: Option[Seq[Anet]] = None,
    cappedCPU: Option[CappedCPU] = None,
    cappedMemory: Option[CappedMemory] = None)

  case class Anet(
    linkname: Option[String] = None,
    lowerLink: Option[String] = None,
    allowedAddress: Option[String] = None,
    configureAllowedAddress: Option[String] = None,
    defrouter: Option[String] = None,
    linkProtection: Option[String] = None,
    macAddress: Option[String] = None)

  case class CappedCPU(ncpus: Option[String] = None)

  case class CappedMemory(
    physical: Option[String] = None,
    swap: Option[String] = None)

}