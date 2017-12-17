scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  "org.scalatest"          %% "scalatest"          % "3.0.4" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

scalaSource in Test := baseDirectory.value / "tests"

enablePlugins(PlayScala, Protoroutes)

PB.protoSources in Compile := Seq(baseDirectory.value / "protobuf")
PB.protoSources in Compile += protoroutesDependencyProtoPath.value

protoroutesPlay26Router := true
