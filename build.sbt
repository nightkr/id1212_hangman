lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  ),
  connectInput in run := true,
  cancelable in Global := true
)

lazy val proto = project.settings(sharedSettings: _*)

lazy val server = project.settings(sharedSettings: _*).dependsOn(proto)

lazy val dumb_client = project.settings(sharedSettings: _*).dependsOn(proto)

lazy val client = project.settings(sharedSettings: _*).dependsOn(proto)

lazy val root = project.in(file(".")).aggregate(proto, server, dumb_client, client)
