import sbtcrossproject.CrossProject
import Dependencies._
import build._

Global / onChangedBuildSource := ReloadOnSourceChanges

json4sSettings(cross = false)
noPublish

lazy val ast = CrossProject(
  id = "json4s-ast",
  base = file("ast"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
    buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
    buildInfoPackage := "org.json4s",
    libraryDependencies ++= Seq(scalatest.value, scalatestScalacheck.value),
  )
  .enablePlugins(BuildInfoPlugin)
  .jsSettings(
    scalajsProjectSettings
  )
  .platformsSettings(JSPlatform, NativePlatform)(
    Compile / unmanagedSourceDirectories += {
      baseDirectory.value.getParentFile / "js_native/src/main/scala"
    }
  )
  .platformsSettings(JVMPlatform, NativePlatform)(
    Compile / unmanagedSourceDirectories += {
      baseDirectory.value.getParentFile / "jvm_native/src/main/scala"
    }
  )

lazy val astJVM = ast.jvm

lazy val scalap = Project(
  id = "json4s-scalap",
  base = file("scalap"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= Seq(jaxbApi, scalatest.value, scalatestScalacheck.value),
)

lazy val xml = CrossProject(
  id = "json4s-xml",
  base = file("xml"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
    libraryDependencies += scalaXml.value,
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(ast)

lazy val xmlJVM = xml.jvm

lazy val core = Project(
  id = "json4s-core",
  base = file("core"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= Seq(paranamer, scalatest.value, scalatestScalacheck.value),
  Test / console / initialCommands := """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(astJVM % "compile;test->test", scalap)

lazy val nativeCore = CrossProject(
  id = "json4s-native-core",
  base = file("native-core"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
    libraryDependencies ++= Seq(scalatest.value, scalatestScalacheck.value),
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(ast % "compile;test->test")

lazy val nativeCoreJVM = nativeCore.jvm

lazy val native = Project(
  id = "json4s-native",
  base = file("native"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= Seq(scalatest.value, scalatestScalacheck.value),
).dependsOn(
  core % "compile;test->test",
  nativeCoreJVM % "compile;test->test",
)

lazy val json4sExt = Project(
  id = "json4s-ext",
  base = file("ext"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= jodaTime,
).dependsOn(core)

lazy val jacksonCore = Project(
  id = "json4s-jackson-core",
  base = file("jackson-core"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= jackson,
).dependsOn(astJVM % "compile;test->test")

lazy val jacksonSupport = Project(
  id = "json4s-jackson",
  base = file("jackson"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= jackson,
).dependsOn(
  core % "compile;test->test",
  native % "test->test",
  jacksonCore % "compile;test->test",
)

lazy val examples = Project(
  id = "json4s-examples",
  base = file("examples"),
).settings(
  json4sSettings(cross = false),
  noPublish,
).dependsOn(
  core % "compile;test->test",
  native % "compile;test->test",
  jacksonSupport % "compile;test->test",
  json4sExt,
  mongo
)

lazy val scalazExt = CrossProject(
  id = "json4s-scalaz",
  base = file("scalaz"),
)(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    json4sSettings(cross = true),
    libraryDependencies += scalatest.value,
    libraryDependencies += scalaz_core.value,
  )
  .jsSettings(
    scalajsProjectSettings
  )
  .dependsOn(
    ast % "compile;test->test",
    nativeCore % "provided->compile",
  )
  .configurePlatform(JVMPlatform)(
    _.dependsOn(
      native % "test",
      jacksonCore % "provided->compile",
    )
  )

lazy val mongo = Project(
  id = "json4s-mongo",
  base = file("mongo"),
).settings(
  json4sSettings(cross = false),
  libraryDependencies ++= Seq(
    scalatest.value,
    "org.mongodb" % "mongo-java-driver" % "3.12.8"
  ),
).dependsOn(
  core % "compile;test->test",
  jacksonSupport % "test",
)

lazy val json4sTests = Project(
  id = "json4s-tests",
  base = file("tests"),
).settings(
  json4sSettings(cross = false),
  noPublish,
  libraryDependencies ++= Seq(scalatest.value, scalatestScalacheck.value),
  Test / console / initialCommands :=
    """
      |import org.json4s._
      |import reflect._
    """.stripMargin,
).dependsOn(
  core % "compile;test->test",
  xmlJVM,
  native % "compile;test->test",
  json4sExt,
  jacksonSupport
)
