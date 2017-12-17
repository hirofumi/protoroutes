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

class Play26RouterGenerator(
  val params: GeneratorParams
) extends ProtoroutesGenerator {

  protected[this] def handleCodeGeneratorRequest(
    request: CodeGeneratorRequest
  ): CodeGeneratorResponse = {
    val builder = CodeGeneratorResponse.newBuilder()
    buildFileDescriptorsFrom(request)
      .filter(hasServiceWithWebApiMethod)
      .map(generateRouterSourceFile)
      .foreach(builder.addFile)
    builder.build
  }

  private[this] def generateRouterSourceFile(
    file: FileDescriptor
  ): CodeGeneratorResponse.File = {
    val builder    = CodeGeneratorResponse.File.newBuilder()
    val objectName = getObjectNameWithoutSuffix(file)
    builder.setName(s"${file.scalaDirectory}/${objectName}Router.scala")
    val printer =
      FunctionalPrinter()
        .add(s"package ${file.scalaPackageName}")
        .newline
        .add(
          "import _root_.javax.inject.Inject",
          "import _root_.play.api.mvc._",
          "import _root_.play.api.routing._",
          "import _root_.play.api.routing.sird._",
          "import _root_.protoroutes.runtime.play26.PbBodyParsers",
          "import _root_.protoroutes.runtime.play26.PbFormat.requestToWriteable",
          "import _root_.scala.concurrent.ExecutionContext",
          "import _root_.scalapb.json4s.JsonFormat"
        )
        .newline
        .print(file.getServices.asScala)(generateRouterClass)
    builder.setContent(printer.result)
    builder.build
  }

  private[this] def generateRouterClass(
    printer: FunctionalPrinter,
    service: ServiceDescriptor
  ): FunctionalPrinter =
    printer
      .add(s"class ${service.name}Router @Inject() (")
      .addIndented(
        s"impl: ${service.objectName}.${service.name},",
        "action: DefaultActionBuilder,",
        "parse: PbBodyParsers",
      )
      .add(")(implicit ec: ExecutionContext) extends SimpleRouter {")
      .newline
      .indented(
        _.add("private[this] val generated: Router =")
          .indented(
            _.add("Router.from {")
              .indented(_.print(getWebApiMethods(service))(generateRoute))
              .add("}")
          )
          .newline
          .add("override def routes: Router.Routes =")
          .indented(_.add("generated.routes"))
      )
      .newline
      .add("}")
      .newline

  private[this] def generateRoute(
    printer: FunctionalPrinter,
    method: MethodDescriptor
  ): FunctionalPrinter = {
    val http = method.getOptions.getExtension(AnnotationsProto.http)
    def actionForRequestBody: PrinterEndo =
      _.add(s"action.async(parse[${method.scalaIn}]) { implicit request =>")
        .addIndented(s"impl.${method.name}(request.body).map(Results.Ok(_))")
        .add("}")
    http.getPatternCase match {
      case PatternCase.POST =>
        printer
          .add(s"""case POST(p"${http.getPost}") =>""")
          .indented(_.call(actionForRequestBody))
      case PatternCase.PUT =>
        printer
          .add(s"""case PUT(p"${http.getPut}") =>""")
          .indented(_.call(actionForRequestBody))
      case _ =>
        printer
    }
  }

}
