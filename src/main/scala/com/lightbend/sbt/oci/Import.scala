package com.lightbend.sbt.oci

import sbt.SettingKey
import scala.concurrent.duration.Duration
import scala.collection.immutable.{Seq, Map}

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

    val hostname = SettingKey[String](
      "oci-hostname",
      "Hostname is the container's host name."
    )

    val mounts = SettingKey[Seq[Mount]](
      "oci-mounts",
      "Mounts profile configuration for adding mounts to the container's filesystem."
    )

    val hooks = SettingKey[Hooks](
      "oci-hooks",
      "Hooks are the commands run at various lifecycle events of the container."
    )

    val annotations = SettingKey[Map[String, String]](
      "oci-annotations",
      "Annotations is an unstructured key value map that may be set by external tools to store and retrieve arbitrary metadata."
    )

    val linux = SettingKey[Linux](
      "oci-linux",
      "Linux is platform specific configuration for Linux based containers."
    )
  }

  case class Platform(os: String, arch: String)

  case class Root(path: String, readonly: Boolean)

  case class Process(
    terminal: Boolean,
    user: User,
    args: Seq[String],
    env: Seq[String],
    cwd: String,
    capabilities: Seq[String],
    rlimits: Seq[Rlimit],
    noNewPriviliges: Boolean,
    apparmorProfile: String,
    selinuxLabel: String
  )

  case class User(uid: Int, gid: Int, additionalGids: Seq[Int])

  case class Rlimit(`type`: String, hard: Long, soft: Long)

  case class Mount(destination: String, `type`: String, source: String, options: Seq[String])

  case class Hooks(preStart: Seq[Hook], postStart: Seq[Hook], postStop: Seq[Hook])

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
    mountLabel: String
  )

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
    gid: Int
  )

  // TODO: Add Seccomp parameters
  case class Seccomp()
}