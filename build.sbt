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
        "com.lihaoyi"          %% "fastparse"                  % "1.0.0",
        "com.thesamet.scalapb" %% "compilerplugin"             % V.scalaPb,
        "com.thesamet.scalapb" %% "scalapb-runtime"            % V.scalaPb    % Test,
        "org.scalacheck"       %% "scalacheck"                 % V.scalaCheck % Test,
        "org.scalatest"        %% "scalatest"                  % V.scalaTest  % Test
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
      addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.14"),
      buildInfoPackage    := "protoroutes",
      moduleName          := "sbt-protoroutes",
      name                := "sbt-protoroutes",
      sbtPlugin           := true,
      scriptedBufferLog   := false,
      scriptedLaunchOpts ++= Seq("-Xmx1024M", s"-Dplugin.version=${version.value}")
    )
    .settings(
      // Workaround for sbt/sbt#3469
      // see also: https://github.com/dotty-staging/dotty/commit/627826444eacb7b8e42696b693bf3a6c0c28d8f9
      scriptedLaunchOpts +=
        s"-Dsbt.boot.directory=${((baseDirectory in ThisBuild).value / ".sbt-scripted").getAbsolutePath}"
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
        "com.thesamet.scalapb" %%% "scalapb-runtime" % V.scalaPb,
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
        "com.thesamet.scalapb"   %% "scalapb-runtime-grpc" % V.scalaPb,
        "io.github.scalapb-json" %% "scalapb-playjson"     % "0.7.0-M1",
        "org.scalatest"          %% "scalatest"            % V.scalaTest % Test,
        "org.scalatestplus.play" %% "scalatestplus-play"   % "3.1.2"     % Test
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

lazy val V = new {
  val scalaCheck = "1.13.5"
  val scalaPb    = "0.7.0-rc7"
  val scalaTest  = "3.0.4"
}

addCommandAlias(
  "validate",
  Seq(
    "scalafmtCheck",
    "test:scalafmtCheck",
    "test",
    "plugin/scripted"
  ).mkString(";", ";", "")
)
