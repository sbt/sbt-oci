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

    val linux = SettingKey[Linux](
      "oci-linux",
      "Linux is platform specific configuration for Linux based containers."
    )

    val solaris = SettingKey[Option[Solaris]](
      "oci-Solaris",
      "Solaris is platform specific configuration for Solaris containers."
    )
  }

  case class Platform(os: String, arch: String)

  case class Root(path: String, readonly: Option[Boolean])

  case class Process(
    terminal: Option[Boolean],
    user: User,
    args: Seq[String],
    env: Option[Seq[String]],
    cwd: String,
    capabilities: Option[Seq[String]],
    rlimits: Option[Seq[Rlimit]],
    noNewPrivileges: Option[Boolean],
    apparmorProfile: Option[String],
    selinuxLabel: Option[String])

  case class User(uid: Int, gid: Int, additionalGids: Seq[Int])

  case class Rlimit(`type`: String, hard: Long, soft: Long)

  case class Mount(destination: String, `type`: String, source: String, options: Option[Seq[String]])

  case class Hooks(prestart: Option[Seq[Hook]], poststart: Option[Seq[Hook]], poststop: Option[Seq[Hook]])

  case class Hook(path: String, args: Seq[String], env: Seq[String], timeout: Duration)

  case class Linux(
    uidMappings: Seq[IdMapping],
    gidMappings: Seq[IdMapping],
    sysctl: Map[String, String],
    resources: Resources,
    cgroupsPath: String,
    namespaces: Seq[Namespace],
    devices: Seq[Device],
    seccomp: Seccomp,
    rootfsPropagation: String,
    maskedPaths: Seq[String],
    readonlyPaths: Seq[String],
    mountLabel: String)

  case class IdMapping(hostId: Int, containerId: Int, size: Int)

  case class Resources()

  case class Namespace(`type`: NamespaceType, path: String)

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
    fileMode: Int, // TODO: Create a `FileMode` adt. Reference: https://golang.org/pkg/os/#FileMode
    uid: Int,
    gid: Int)

  // TODO: Add Seccomp parameters
  case class Seccomp()

  case class Solaris(
    milestone: Option[String],
    limitpriv: Option[String],
    maxShmMemory: Option[String],
    anet: Option[Seq[Anet]],
    cappedCPU: Option[CappedCPU],
    cappedMemory: Option[CappedMemory])

  case class Anet(
    linkname: Option[String],
    lowerLink: Option[String],
    allowedAddress: Option[String],
    configureAllowedAddress: Option[String],
    defrouter: Option[String],
    linkProtection: Option[String],
    macAddress: Option[String])

  case class CappedCPU(ncpus: Option[String])

  case class CappedMemory(
    physical: Option[String],
    swap: Option[String])

}