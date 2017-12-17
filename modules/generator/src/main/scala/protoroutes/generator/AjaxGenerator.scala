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
import scalapb.compiler.{FunctionalPrinter, GeneratorParams}

class AjaxGenerator(
  val params: GeneratorParams
) extends ProtoroutesGenerator {

  protected[this] def handleCodeGeneratorRequest(
    request: CodeGeneratorRequest
  ): CodeGeneratorResponse = {
    val builder = CodeGeneratorResponse.newBuilder()
    buildFileDescriptorsFrom(request)
      .filter(hasServiceWithWebApiMethod)
      .map(generateAjaxSourceFile)
      .foreach(builder.addFile)
    builder.build
  }

  private[this] def generateAjaxSourceFile(
    file: FileDescriptor
  ): CodeGeneratorResponse.File = {
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
  ): FunctionalPrinter = {
    val methods = getWebApiMethods(service)
    val name    = s"${service.getName}Ajax"
    printer
      .add(
        s"""final case class $name(baseUrl: String = "", timeout: Int = 0, headers: Map[String, String] = Map.empty, withCredentials: Boolean = false) {"""
      )
      .indented(_.print(methods)({ (p, m) =>
        p.add(
            s"val ${m.name}: PbAjax[_root_.${m.scalaIn}, _root_.${m.scalaOut}] ="
          )
          .indented(_.call(generateAjaxCall(m)))
      }))
      .add("}")
      .newline
  }

  private[this] def generateAjaxCall(method: MethodDescriptor): PrinterEndo = {
    printer =>
      val http = method.getOptions.getExtension(AnnotationsProto.http)
      http.getPatternCase match {
        case PatternCase.POST =>
          printer.add(
            s"""new PbAjax[_root_.${method.scalaIn}, _root_.${method.scalaOut}]("POST", baseUrl + "${http.getPost}", timeout, headers, withCredentials)"""
          )
        case PatternCase.PUT =>
          printer.add(
            s"""new PbAjax[_root_.${method.scalaIn}, _root_.${method.scalaOut}]("PUT", baseUrl + "${http.getPut}", timeout, headers, withCredentials)"""
          )
        case _ =>
          printer
      }
  }

}
