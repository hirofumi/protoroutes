package protoroutes

import java.io.File
import protocbridge.Target
import protoroutes.generator.{DotProto, Generators}
import sbt._
import sbt.Keys._
import sbtprotoc.ProtocPlugin
import sbtprotoc.ProtocPlugin.autoImport.PB
import scalapb.compiler.GeneratorParams
import scalapb.compiler.Version.scalapbVersion

object Protoroutes extends AutoPlugin {

  object gen extends Generators

  object autoImport {

    val protoroutesAjax: SettingKey[Boolean] =
      settingKey("Whether or not to generate Scala.js Ajax client")

    val protoroutesAjaxGeneratorParams: SettingKey[GeneratorParams] =
      settingKey("GeneratorParams used to generate Scala.js Ajax client")

    val protoroutesDependencyProtoPath: SettingKey[File] =
      settingKey("Path to output the generated dependency .proto files")

    val protoroutesOutputPath: SettingKey[File] =
      settingKey("Default path to output the generated files")

    val protoroutesPlay26Router: SettingKey[Boolean] =
      settingKey("Whether or not to generate Play 2.6 Router")

    val protoroutesPlay26RouterGeneratorParams: SettingKey[GeneratorParams] =
      settingKey("GeneratorParams used to generate Play 2.6 Router")

    val protoroutesTargets: SettingKey[Seq[Target]] =
      settingKey("List of targets to generate")

  }

  override def requires: Plugins =
    ProtocPlugin

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      protoroutesAjax :=
        protoroutesAjax.?.value.getOrElse(false),
      protoroutesAjaxGeneratorParams :=
        protoroutesAjaxGeneratorParams.?.value
          .getOrElse(GeneratorParams(flatPackage = true)),
      protoroutesDependencyProtoPath :=
        protoroutesDependencyProtoPath.?.value
          .getOrElse(target.value / "protoroutes_dependency"),
      protoroutesOutputPath :=
        protoroutesOutputPath.?.value
          .getOrElse((sourceManaged in Compile).value),
      protoroutesPlay26Router :=
        protoroutesPlay26Router.?.value
          .getOrElse(false),
      protoroutesPlay26RouterGeneratorParams :=
        protoroutesPlay26RouterGeneratorParams.?.value
          .getOrElse(GeneratorParams(flatPackage = true, grpc = true)),
      protoroutesTargets :=
        protoroutesTargets.?.value.getOrElse(Nil)
    ) ++ Seq(
      PB.generate in Compile :=
        (PB.generate in Compile)
          .dependsOn(
            Def.task(
              generateDependencyProtoFiles(protoroutesDependencyProtoPath.value)
            )
          )
          .value,
      PB.protoSources in Compile +=
        protoroutesDependencyProtoPath.value,
      PB.targets in Compile ++=
        protoroutesTargets.value
    ) ++ Seq(
      libraryDependencies ++=
        when(protoroutesAjax.value)(
          "com.github.hirofumi"  %% "protoroutes-runtime-ajax_sjs0.6" % BuildInfo.version,
          "com.thesamet.scalapb" %% "scalapb-runtime_sjs0.6"          % scalapbVersion % "compile,protobuf"
        ),
      protoroutesTargets ++=
        when(protoroutesAjax.value)(
          gen
            .ajax(protoroutesAjaxGeneratorParams.value)
            .map(Target(_, protoroutesOutputPath.value))
        ).flatten,
    ) ++ Seq(
      libraryDependencies ++= {
        val grpc =
          if (protoroutesPlay26RouterGeneratorParams.value.grpc) "-grpc" else ""
        when(protoroutesPlay26Router.value)(
          "com.github.hirofumi"  %% "protoroutes-runtime-play26" % BuildInfo.version,
          "com.thesamet.scalapb" %% s"scalapb-runtime$grpc"      % scalapbVersion % "compile,protobuf"
        )
      },
      protoroutesTargets ++=
        when(protoroutesPlay26Router.value)(
          gen
            .play26Router(protoroutesPlay26RouterGeneratorParams.value)
            .map(Target(_, protoroutesOutputPath.value))
        ).flatten
    )

  private[this] def generateDependencyProtoFiles(file: File): Unit =
    for (dotProto <- Seq(DotProto.annotations, DotProto.http)) {
      IO.write(file / dotProto.path, dotProto.source)
    }

  private[this] def when[A](cond: Boolean)(as: A*): Seq[A] =
    if (cond) as else Nil

}
