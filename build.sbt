import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype._

lazy val protoroutes =
  project
    .in(file("."))
    .aggregate(
      generator,
      plugin,
      `runtime-ajax`,
      `runtime-play26`
    )
    .settings(settings ++ noPublish)
    .settings(name := "protoroutes")

lazy val generator =
  project
    .in(file("modules/generator"))
    .aggregate(`runtime-ajax`, `runtime-play26`)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        "com.google.api.grpc"  %  "proto-google-common-protos" % "1.0.3",
        "com.thesamet.scalapb" %% "compilerplugin"             % "0.7.0-rc6"
      )
    )

lazy val plugin =
  project
    .in(file("modules/plugin"))
    .aggregate(generator)
    .dependsOn(generator)
    .enablePlugins(BuildInfoPlugin)
    .settings(settings)
    .settings(
      addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.13"),
      buildInfoPackage   := "protoroutes",
      moduleName         := "sbt-protoroutes",
      name               := "sbt-protoroutes",
      sbtPlugin          := true,
      scriptedBufferLog  := false,
      scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    )

lazy val `runtime-ajax` =
  project
    .in(file("modules/runtime-ajax"))
    .enablePlugins(ScalaJSPlugin)
    .settings(settings)
    .settings(
      jsEnv := new JSDOMNodeJSEnv()
    )
    .settings(
      libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %%% "scalapb-runtime" % "0.7.0-rc6",
        "org.scala-js"         %%% "scalajs-dom"     % "0.9.4"
      )
    )

lazy val `runtime-play26` =
  project
    .in(file("modules/runtime-play26"))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.play"      %% "play"                 % "2.6.10",
        "com.thesamet.scalapb"   %% "scalapb-runtime-grpc" % "0.7.0-rc6",
        "com.thesamet.scalapb"   %% "scalapb-json4s"       % "0.7.0-rc1",
        "org.scalatest"          %% "scalatest"            % "3.0.4" % Test,
        "org.scalatestplus.play" %% "scalatestplus-play"   % "3.1.2" % Test
      )
    )

lazy val settings: Seq[Def.Setting[_]] =
  Seq(
    name              := s"protoroutes ${thisProject.value.id}",
    organization      := "com.github.hirofumi",
    publishMavenStyle := true,
    publishTo         := sonatypePublishTo.value,
    scalaVersion      := "2.12.4"
  ) ++ Seq(
    licenses := Seq(
      "MIT" -> url("https://opensource.org/licenses/MIT")
    ),
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommand("publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-target:jvm-1.8",
      "-unchecked",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard"
    ),
    scalacOptions in (Compile, compile) ++= Seq(
      "-Xfatal-warnings",
      "-Xlint",
      "-Ywarn-unused-import"
    ),
    scalacOptions in (Compile, console) ++= Seq(
      "-Xlint:-unused"
    ),
    sonatypeProjectHosting := Some(
      GithubHosting("hirofumi", "protoroutes", "hirofummy@gmail.com")
    ),
    testOptions ++= Seq(
      Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "2"),
      Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
    )
  )

lazy val noPublish: Seq[Def.Setting[_]] =
  Seq(
    publishArtifact := false,
    publishLocal    := {},
    publish         := {}
  )
