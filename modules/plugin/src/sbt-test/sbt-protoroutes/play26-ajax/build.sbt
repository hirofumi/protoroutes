lazy val client =
  project
    .enablePlugins(Protoroutes, ScalaJSPlugin, ScalaJSWeb)
    .settings(commonSettings)
    .settings(
      protoroutesAjax                 := true,
      scalaJSUseMainModuleInitializer := true
    )

lazy val server =
  project
    .enablePlugins(PlayScala, Protoroutes, SbtWeb)
    .settings(commonSettings)
    .settings(
      pipelineStages in Assets := Seq(scalaJSPipeline),
      protoroutesPlay26Router  := true,
      scalaJSProjects          := Seq(client)
    )
    .settings(
      libraryDependencies ++= Seq(
        guice,
        "com.vmunier"            %% "scalajs-scripts"    % "1.1.1",
        "org.scalatest"          %% "scalatest"          % "3.0.4" % Test,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
      )
    )

lazy val commonSettings =
  Seq(
    PB.protoSources in Compile := Seq(baseDirectory.value.getParentFile / "protobuf"),
    PB.protoSources in Compile += protoroutesDependencyProtoPath.value,
    scalaVersion               := "2.12.4"
  )
