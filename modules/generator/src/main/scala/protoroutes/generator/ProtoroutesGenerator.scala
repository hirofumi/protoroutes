package protoroutes.generator

import com.google.api.AnnotationsProto
import com.google.protobuf.Descriptors.ServiceDescriptor
import com.google.protobuf.{Descriptors, ExtensionRegistry}
import com.google.protobuf.compiler.PluginProtos.{
  CodeGeneratorRequest,
  CodeGeneratorResponse
}
import protocbridge.{JvmGenerator, ProtocCodeGenerator}
import scala.collection.JavaConverters._
import scala.collection.{breakOut, mutable}
import scalapb.compiler.{DescriptorPimps, StreamType}

trait ProtoroutesGenerator
  extends ProtocCodeGenerator with DescriptorPimps with FunctionalPrinterPimps {

  def name: String =
    getClass.getName.replaceAll("([^.]+\\.)*", "").replaceAll("Generator$", "")

  def registry: ExtensionRegistry =
    ExtensionRegistries.default

  final def run(request: Array[Byte]): Array[Byte] =
    handleCodeGeneratorRequest(
      CodeGeneratorRequest.parseFrom(request, registry)
    ).toByteArray

  final def toJvmGenerator: JvmGenerator =
    JvmGenerator(s"protoroutes_$name", this)

  protected[this] def handleCodeGeneratorRequest(
    request: CodeGeneratorRequest
  ): CodeGeneratorResponse

  protected[this] def buildFileDescriptorsFrom(
    request: CodeGeneratorRequest
  ): Seq[Descriptors.FileDescriptor] = {
    val nameToFile = mutable.AnyRefMap.empty[String, Descriptors.FileDescriptor]
    for (proto <- request.getProtoFileList.asScala) {
      nameToFile.update(
        proto.getName,
        Descriptors.FileDescriptor.buildFrom(
          proto,
          proto.getDependencyList.asScala.map(nameToFile)(breakOut)
        )
      )
    }
    request.getFileToGenerateList.asScala.map(nameToFile)
  }

  protected[this] def getObjectNameWithoutSuffix(
    file: Descriptors.FileDescriptor
  ): String =
    file.fileDescriptorObjectName.replaceAll("Proto$", "")

  protected[this] def getWebApiMethods(
    service: ServiceDescriptor
  ): Seq[Descriptors.MethodDescriptor] =
    service.getMethods.asScala.filter(isWebApiMethod)

  protected[this] def hasServiceWithWebApiMethod(
    file: Descriptors.FileDescriptor
  ): Boolean =
    file.getServices.asScala.exists(getWebApiMethods(_).nonEmpty)

  protected[this] def isWebApiMethod(
    method: Descriptors.MethodDescriptor
  ): Boolean =
    method.streamType == StreamType.Unary &&
      method.getOptions.hasExtension(AnnotationsProto.http)

}
