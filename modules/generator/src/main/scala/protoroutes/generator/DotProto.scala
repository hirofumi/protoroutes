package protoroutes.generator

import com.google.api.{AnnotationsProto, HttpProto}
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.{Label, Type}
import com.google.protobuf.DescriptorProtos.{
  DescriptorProto,
  FieldDescriptorProto,
  FileDescriptorProto
}
import protoroutes.generator.FunctionalPrinterPimps._
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scalapb.compiler.FunctionalPrinter
import scalapb.compiler.FunctionalPrinter.PrinterEndo

final class DotProto private (
  proto: FileDescriptorProto,
  header: PrinterEndo
) {

  def path: String =
    proto.getName

  def source: String =
    toPrinterEndo
      .apply(FunctionalPrinter())
      .result()

  def toPrinterEndo: PrinterEndo = { printer =>
    printer
      .add(s"// DO NOT EDIT: generated from ${proto.getClass}")
      .newline
      .call(header)
      .newline
      .when(proto.hasSyntax)(
        _.add(s"""syntax = "${proto.getSyntax}";""").newline
      )
      .when(proto.hasPackage)(
        _.add(s"package ${proto.getPackage};").newline
      )
      .when(!proto.getDependencyList.isEmpty)(
        _.print(proto.getDependencyList.asScala)(
          (p, d) => p.add(s"""import "$d";""")
        ).newline
      )
      .when(proto.hasOptions)(
        _.print(
          proto.getOptions.getAllFields.asScala.toSeq.sortBy(_._1.getName)
        )({
          case (p, (k, v: java.lang.Boolean)) =>
            p.add(s"option ${k.getName} = $v;")
          case (p, (k, v)) if k.getName == "java_package" =>
            p.add(s"""option ${k.getName} = "protoroutes.shaded.$v";""")
          case (p, (k, v)) =>
            p.add(s"""option ${k.getName} = "$v";""")
        }).newline
      )
      .when(!proto.getExtensionList.isEmpty)(
        _.print(proto.getExtensionList.asScala)(extensionPrinter).newline
      )
      .when(!proto.getMessageTypeList.isEmpty)(
        _.print(proto.getMessageTypeList.asScala)(messageTypePrinter).newline
      )
  }

  private[this] def extensionPrinter(
    printer: FunctionalPrinter,
    e: FieldDescriptorProto
  ): FunctionalPrinter =
    printer
      .add(s"extend ${normalizeTypeName(e.getExtendee)} {")
      .addIndented(s"${getTypeName(e)} ${e.getName} = ${e.getNumber};")
      .add("}")

  private[this] def messageTypePrinter(
    printer: FunctionalPrinter,
    m: DescriptorProto
  ): FunctionalPrinter =
    printer
      .add(s"message ${m.getName} {")
      .indented(_.call(messageFieldsPrinter(_, m)))
      .add("}")

  private[this] def messageFieldsPrinter(
    printer: FunctionalPrinter,
    m: DescriptorProto
  ): FunctionalPrinter = {
    @tailrec def go(
      p: FunctionalPrinter,
      fs: Seq[FieldDescriptorProto],
      oneofs: Map[Int, Seq[FieldDescriptorProto]]
    ): FunctionalPrinter =
      fs match {
        case f +: rest =>
          if (f.hasOneofIndex) {
            val i = f.getOneofIndex
            oneofs.get(i) match {
              case Some(o) =>
                go(
                  p.add(s"oneof ${m.getOneofDecl(i).getName} {")
                    .indented(_.print(o)(messageFieldPrinter))
                    .add("}"),
                  rest,
                  oneofs - i
                )
              case None =>
                go(p, rest, oneofs)
            }
          } else {
            go(p.call(messageFieldPrinter(_, f)), rest, oneofs)
          }
        case Seq() =>
          p
      }
    val fields = m.getFieldList.asScala
    go(printer, fields, fields.filter(_.hasOneofIndex).groupBy(_.getOneofIndex))
  }

  private[this] def messageFieldPrinter(
    printer: FunctionalPrinter,
    field: FieldDescriptorProto
  ): FunctionalPrinter = {
    val isProto2 =
      proto.getSyntax == "proto2"
    val label =
      (if (field.hasLabel) Some(field.getLabel) else None) match {
        case Some(Label.LABEL_OPTIONAL) => if (isProto2) "optional " else ""
        case Some(Label.LABEL_REPEATED) => "repeated "
        case Some(Label.LABEL_REQUIRED) => "required "
        case None                       => ""
      }
    val fieldType =
      field.getType match {
        case Type.TYPE_BOOL     => "bool"
        case Type.TYPE_BYTES    => "bytes"
        case Type.TYPE_DOUBLE   => "double"
        case Type.TYPE_ENUM     => getTypeName(field)
        case Type.TYPE_FIXED32  => "fixed32"
        case Type.TYPE_FIXED64  => "fixed64"
        case Type.TYPE_FLOAT    => "float"
        case Type.TYPE_GROUP    => assert(false, "unsupported")
        case Type.TYPE_INT32    => "int32"
        case Type.TYPE_INT64    => "int64"
        case Type.TYPE_MESSAGE  => getTypeName(field)
        case Type.TYPE_SFIXED32 => "sfixed32"
        case Type.TYPE_SFIXED64 => "sfixed64"
        case Type.TYPE_SINT32   => "sint32"
        case Type.TYPE_SINT64   => "sint64"
        case Type.TYPE_STRING   => "string"
        case Type.TYPE_UINT32   => "uint32"
        case Type.TYPE_UINT64   => "uint64"
      }
    printer.add(s"$label$fieldType ${field.getName} = ${field.getNumber};")
  }

  private[this] def getTypeName(field: FieldDescriptorProto): String =
    normalizeTypeName(field.getTypeName)

  private[this] def normalizeTypeName(name: String): String =
    if (proto.hasPackage) {
      name.replaceFirst(s"^\\.(?:${Regex.quote(s"${proto.getPackage}.")})?", "")
    } else {
      name.replaceFirst("^\\.", "")
    }

}

object DotProto {

  private[this] val fromGoogle: PrinterEndo =
    _.add(
      """// LICENSE OF THE ORIGINAL .proto file:""",
      """//""",
      """// Copyright 2017 Google LLC""",
      """//""",
      """// Licensed under the Apache License, Version 2.0 (the "License");""",
      """// you may not use this file except in compliance with the License.""",
      """// You may obtain a copy of the License at""",
      """//""",
      """//     http://www.apache.org/licenses/LICENSE-2.0""",
      """//""",
      """// Unless required by applicable law or agreed to in writing, software""",
      """// distributed under the License is distributed on an "AS IS" BASIS,""",
      """// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.""",
      """// See the License for the specific language governing permissions and""",
      """// limitations under the License."""
    )

  val annotations: DotProto =
    new DotProto(AnnotationsProto.getDescriptor.toProto, fromGoogle)

  val http: DotProto =
    new DotProto(HttpProto.getDescriptor.toProto, fromGoogle)

}
