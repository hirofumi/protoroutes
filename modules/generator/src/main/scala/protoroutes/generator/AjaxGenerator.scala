package protoroutes.generator

import com.google.api.AnnotationsProto
import com.google.api.HttpRule.PatternCase
import com.google.protobuf.Descriptors.{
  FileDescriptor,
  MethodDescriptor,
  ServiceDescriptor
}
import com.google.protobuf.compiler.PluginProtos.{
  CodeGeneratorRequest,
  CodeGeneratorResponse
}
import scala.collection.JavaConverters._
import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.compiler.{
  DescriptorImplicits,
  FunctionalPrinter,
  GeneratorParams
}

class AjaxGenerator(params: GeneratorParams) extends ProtoroutesGenerator {

  protected[this] def handleCodeGeneratorRequest(
    request: CodeGeneratorRequest
  ): CodeGeneratorResponse = {
    val nameToFile = buildFileDescriptorsFrom(request)
    implicit val implicits: DescriptorImplicits =
      new DescriptorImplicits(params, nameToFile.values.toVector)
    val builder = CodeGeneratorResponse.newBuilder()
    request.getFileToGenerateList.asScala
      .map(nameToFile)
      .filter(hasServiceWithWebApiMethod)
      .map(generateAjaxSourceFile)
      .foreach(builder.addFile)
    builder.build
  }

  private[this] def generateAjaxSourceFile(
    file: FileDescriptor
  )(implicit implicits: DescriptorImplicits): CodeGeneratorResponse.File = {
    import implicits._
    val builder    = CodeGeneratorResponse.File.newBuilder()
    val objectName = getObjectNameWithoutSuffix(file)
    builder.setName(s"${file.scalaDirectory}/${objectName}Ajax.scala")
    val printer =
      FunctionalPrinter()
        .add(s"package ${file.scalaPackageName}")
        .newline
        .add(
          "import _root_.protoroutes.runtime.ajax.PbAjax",
          "import _root_.scala.scalajs.concurrent.JSExecutionContext.Implicits.queue"
        )
        .newline
        .print(file.getServices.asScala)(generateAjaxClass)
    builder.setContent(printer.result)
    builder.build
  }

  private[this] def generateAjaxClass(
    printer: FunctionalPrinter,
    service: ServiceDescriptor
  )(implicit implicits: DescriptorImplicits): FunctionalPrinter = {
    import implicits._
    val methods = getWebApiMethods(service)
    val name    = s"${service.getName}Ajax"
    printer
      .add(
        s"""final case class $name(baseUrl: String = "", timeout: Int = 0, headers: Map[String, String] = Map.empty, withCredentials: Boolean = false) {"""
      )
      .indented(_.print(methods)({ (p, m) =>
        p.add(s"val ${m.name}: ${pbAjaxType(m)} =")
          .indented(_.call(generateAjaxCall(m)))
      }))
      .add("}")
      .newline
  }

  private[this] def generateAjaxCall(
    method: MethodDescriptor
  )(implicit implicits: DescriptorImplicits): PrinterEndo = { printer =>
    val http = method.getOptions.getExtension(AnnotationsProto.http)
    http.getPatternCase match {
      case PatternCase.POST =>
        printer.add(
          s"""new ${pbAjaxType(method)}("POST", baseUrl + "${http.getPost}", timeout, headers, withCredentials)"""
        )
      case PatternCase.PUT =>
        printer.add(
          s"""new ${pbAjaxType(method)}("PUT", baseUrl + "${http.getPut}", timeout, headers, withCredentials)"""
        )
      case _ =>
        printer
    }
  }

  private[this] def pbAjaxType(
    method: MethodDescriptor
  )(implicit implicits: DescriptorImplicits): String = {
    import implicits._
    s"PbAjax[${method.inputType.scalaType}, ${method.outputType.scalaType}]"
  }

}
