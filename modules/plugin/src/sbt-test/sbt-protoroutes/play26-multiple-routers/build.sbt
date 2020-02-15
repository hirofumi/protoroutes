scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  guice,
  "org.scalatest"          %% "scalatest"          % "3.0.8" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % Test
)

scalaSource in Test := baseDirectory.value / "tests"

enablePlugins(PlayScala, Protoroutes)

PB.protoSources in Compile := Seq(baseDirectory.value / "protobuf")
PB.protoSources in Compile += protoroutesDependencyProtoPath.value

protoroutesPlay26Router := true
