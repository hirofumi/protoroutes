package protoroutes.generator

import protocbridge.JvmGenerator
import scalapb.compiler.GeneratorParams

abstract class Generators {

  def ajax(params: GeneratorParams): Seq[(JvmGenerator, Seq[String])] =
    withScalaPb(params, "Ajax", new AjaxGenerator(_))

  def play26Router(params: GeneratorParams): Seq[(JvmGenerator, Seq[String])] =
    withScalaPb(params, "Play26Router", new Play26RouterGenerator(_))

  private[this] def withScalaPb(
    params: GeneratorParams,
    name: String,
    newGenerator: GeneratorParams => ProtoroutesGenerator
  ): Seq[(JvmGenerator, Seq[String])] = {
    val target =
      scalapb.gen(
        flatPackage        = params.flatPackage,
        javaConversions    = params.javaConversions,
        grpc               = params.grpc,
        singleLineToString = params.singleLineToString
      )
    Seq(newGenerator(params).toJvmGenerator -> target._2, target)
  }

}
