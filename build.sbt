import sbtcrossproject.CrossProject
import Dependencies._
import build._

Global / onChangedBuildSource := ReloadOnSourceChanges

json4sSettings()
noPublish

lazy val ast = CrossProject(
  id = "json4s-ast",
  base = file("ast"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
    buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.json4s",
  )
  .enablePlugins(BuildInfoPlugin)

lazy val astJVM = ast.jvm

lazy val scalap = CrossProject(
  id = "json4s-scalap",
  base = file("scalap"),
)(JVMPlatform, JSPlatform, NativePlatform)
.settings(
  json4sSettings(cross = true),
)

lazy val xml = CrossProject(
  id = "json4s-xml",
  base = file("xml"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies += scalaXml.value,
).dependsOn(core)

lazy val core = CrossProject(
  id = "json4s-core",
  base = file("core"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies ++= Seq(paranamer),
  Test / console / initialCommands := """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(ast % "compile;test->test", scalap)

lazy val nativeCore = CrossProject(
  id = "json4s-native-core",
  base = file("native-core"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
  )
  .dependsOn(ast % "compile;test->test")

lazy val nativeCoreJVM = nativeCore.jvm

lazy val native = CrossProject(
  id = "json4s-native",
  base = file("native"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
).dependsOn(
  core % "compile;test->test",
  nativeCore % "compile;test->test",
)

lazy val json4sExt = CrossProject(
  id = "json4s-ext",
  base = file("ext"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies ++= jodaTime,
).dependsOn(native % "provided->compile;test->test")

lazy val jacksonCore = CrossProject(
  id = "json4s-jackson-core",
  base = file("jackson-core"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies ++= jackson,
).dependsOn(ast % "compile;test->test")

lazy val jacksonSupport = CrossProject(
  id = "json4s-jackson",
  base = file("jackson"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies ++= jackson,
).dependsOn(
  jacksonCore % "compile;test->test",
  core % "compile;test->test",
)

lazy val scalazExt = CrossProject(
  id = "json4s-scalaz",
  base = file("scalaz"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies += scalaz_core.value,
).dependsOn(
  ast % "compile;test->test",
  nativeCore % "provided->compile",
  jacksonCore % "provided->compile",
)

lazy val mongo = CrossProject(
  id = "json4s-mongo",
  base = file("mongo"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  libraryDependencies ++= Seq(
    "org.mongodb" % "mongo-java-driver" % "3.12.8"
  ),
).dependsOn(core % "compile;test->test")

lazy val json4sTests = CrossProject(
  id = "json4s-tests",
  base = file("tests"),
)(JVMPlatform, JSPlatform, NativePlatform).settings(
  json4sSettings(cross = true),
  noPublish,
  libraryDependencies ++= Seq(scalatest, mockito, jaxbApi, scalatestScalacheck.value),
  Test / console / initialCommands :=
    """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(core, xml, native, json4sExt, scalazExt, jacksonSupport, mongo)
