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

  case class Resources(
    devices: Seq[DeviceCgroup] = Seq.empty,
    disableOOMKiller: Option[Boolean] = None,
    oomScoreAdj: Option[Int] = None,
    memory: Option[Memory] = None,
    cpu: Option[Cpu] = None,
    pids: Option[Pids] = None,
    blockIO: Option[BlockIO] = None,
    hugepageLimits: Option[Seq[HugepageLimit]] = None,
    network: Option[Network] = None
  )

  case class DeviceCgroup(
    allow: Boolean,
    `type`: Option[String] = None,
    major: Option[Long] = None,
    minor: Option[Long] = None,
    access: Option[String] = None
  )

  case class Memory(
    limit: Option[Long] = None,
    reservation: Option[Long] = None,
    swap: Option[Long] = None,
    kernel: Option[Long] = None,
    kernelTCP: Long,
    swappiness: Option[Long] = None
  )

  case class Cpu(
    shares: Option[Long] = None,
    quota: Option[Long] = None,
    period: Option[Long] = None,
    realtimeRuntime: Option[Long] = None,
    realtimePeriod: Option[Long] = None,
    cpus: Option[String] = None,
    mems: Option[String] = None
  )

  case class Pids(limit: Option[Long])

  case class BlockIO(
    blkioWeight: Option[Short] = None,
    blkioLeafWeight: Option[Short] = None,
    blkioWeightDevice: Option[Seq[WeightDevice]] = None,
    blkioThrottleReadBpsDevice: Option[Seq[ThrottleDevice]] = None,
    blkioThrottleWriteBpsDevice: Option[Seq[ThrottleDevice]] = None,
    blkioThrottleReadIOPSDevice: Option[Seq[ThrottleDevice]] = None,
    blkioThrottleWriteIOPSDevice: Option[Seq[ThrottleDevice]] = None
  )

  private trait BlockIODevice {
    def major: Long
    def minor: Long
  }

  case class WeightDevice(
    major: Long,
    minor: Long,
    weight: Option[Short] = None,
    leafWeight: Option[Short] = None) extends BlockIODevice

  case class ThrottleDevice(
    major: Long,
    minor: Long,
    rate: Option[Long] = None
  )

  case class HugepageLimit(pageSize: Option[String] = None, limit: Option[Long] = None)

  case class Network(classID: Int, priorities: Option[Seq[InterfacePriority]])

  case class InterfacePriority(name: String, priority: Int)

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

  case class Seccomp(
    defaultAction: Action,
    architectures: Seq[Architecture],
    syscalls: Option[Seq[Syscall]]
  )

  sealed trait Action

  object Action {
    case object Kill extends Action { override def toString = "SCMP_ACT_KILL" }
    case object Trap extends Action { override def toString = "SCMP_ACT_TRAP" }
    case object Errno extends Action { override def toString = "SCMP_ACT_ERRNO" }
    case object Trace extends Action { override def toString = "SCMP_ACT_TRACE" }
    case object Allow extends Action { override def toString = "SCMP_ACT_ALLOW" }
  }

  sealed trait Architecture

  object Architecture {
    case object X86 extends Architecture { override def toString = "SCMP_ARCH_X86" }
    case object X86_64 extends Architecture { override def toString = "SCMP_ARCH_X86_64" }
    case object X32 extends Architecture { override def toString = "SCMP_ARCH_X32" }
    case object ARM extends Architecture { override def toString = "SCMP_ARCH_ARM" }
    case object AARCH64 extends Architecture { override def toString = "SCMP_ARCH_AARCH64" }
    case object MIPS extends Architecture { override def toString = "SCMP_ARCH_MIPS" }
    case object MIPS64 extends Architecture { override def toString = "SCMP_ARCH_MIPS64" }
    case object MIPS64N32 extends Architecture { override def toString = "SCMP_ARCH_MIPS64N32" }
    case object MIPSEL extends Architecture { override def toString = "SCMP_ARCH_MIPSEL" }
    case object MIPSEL64 extends Architecture { override def toString = "SCMP_ARCH_MIPESEL64" }
    case object MIPSEL64N32 extends Architecture { override def toString = "SCMP_ARCH_MIPSEL63N32" }
  }

  case class Syscall(name: String, action: Action, args: Option[Seq[Arg]])

  case class Arg(index: Int, value: Long, valueTwo: Long, op: Operator)

  sealed trait Operator

  object Operator {
    case object NotEqual extends Operator { override def toString = "SCMP_CMP_NE" }
    case object LessThan extends Operator { override def toString = "SCMP_CMP_LT" }
    case object LessEqual extends Operator { override def toString = "SCMP_CMP_LE" }
    case object EqualTo extends Operator { override def toString = "SCMP_CMP_EQ" }
    case object GreaterEqual extends Operator { override def toString = "SCMP_CMP_GE" }
    case object GreaterThan extends Operator { override def toString = "SCMP_CMP_GT" }
    case object MaskedEqual extends Operator { override def toString = "SCMP_CMP_MASKED_EQ" }
  }

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