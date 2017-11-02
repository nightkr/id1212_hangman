lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  ),
  fork in run := true,
  connectInput in run := true,
  cancelable in Global := true
)

lazy val proto = project.settings(sharedSettings: _*)

lazy val server = project.settings(sharedSettings: _*).dependsOn(proto)

lazy val client = project.settings(sharedSettings: _*).dependsOn(proto)

lazy val root = project.in(file(".")).aggregate(proto, server, client)
