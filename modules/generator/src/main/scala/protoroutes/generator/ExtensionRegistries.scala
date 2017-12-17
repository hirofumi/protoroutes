package protoroutes.generator

import com.google.api.AnnotationsProto
import com.google.protobuf.ExtensionRegistry
import scalapb.options.compiler.Scalapb

object ExtensionRegistries {

  val default: ExtensionRegistry = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)
    AnnotationsProto.registerAllExtensions(registry)
    registry.getUnmodifiable
  }

}
