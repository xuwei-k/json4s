addSbtPlugin("ch.epfl.lamp"     % "sbt-dotty"            % "0.5.3")
addSbtPlugin("com.typesafe"     % "sbt-mima-plugin"      % "0.8.1")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"        % "0.10.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.6")
addSbtPlugin("com.github.sbt"   % "sbt-pgp"              % "2.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.5.2")
addSbtPlugin("com.github.sbt"   % "sbt-release"          % "1.0.15")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

