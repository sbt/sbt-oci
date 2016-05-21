import scalariform.formatter.preferences._

sbtPlugin := true

organization := "com.lightbend.sbt"
name := "sbt-oci"

scalaVersion := "2.10.4"
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

scalariformSettings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

releaseSettings
ReleaseKeys.versionBump := sbtrelease.Version.Bump.Minor

libraryDependencies ++= List(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

scriptedSettings
scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }
