package protoroutes.generator.httprule

import com.google.protobuf.descriptor.DescriptorProto
import fastparse.all._
import scalapb.GeneratedMessageCompanion
import scalapb.descriptors.Descriptor

final class TemplateParsers[
  A <: scalapb.GeneratedMessage with scalapb.Message[A]
](
  implicit A: GeneratedMessageCompanion[A]
) {

  def go(descriptor: Descriptor, companion: GeneratedMessageCompanion[_], fieldPath: FieldPath): Seq[FieldPath] = {
    companion.scalaDescriptor.fields
      .flatMap({ field =>
        println(fieldPath.copy(tail = fieldPath.tail :+ field.name).text)
        if (field.protoType.isTypeMessage && !field.isMapField && field.asProto.companion.scalaDescriptor.asProto != descriptor.asProto) {
          if (field.asProto.companion.scalaDescriptor.asProto == descriptor.asProto) {
            println(s"boo! ${field.asProto.companion.scalaDescriptor}")
          }
          go(
            field.asProto.companion.scalaDescriptor,
            companion.messageCompanionForFieldNumber(field.number),
            fieldPath.copy(tail = fieldPath.tail :+ field.name)
          )
        } else {
          FieldPath(field.name, Nil) :: Nil
        }
      })
  }
  val validFieldPaths: Seq[FieldPath] =
    A.scalaDescriptor.fields
      .flatMap({ field =>
        if (field.protoType.isTypeMessage && !field.isMapField) {
//          println(field.name)
//          println(field.asProto)
          go(
            field.asProto.companion.scalaDescriptor,
            A.messageCompanionForFieldNumber(field.number),
            FieldPath(field.name, Nil)
          )
        } else {
          FieldPath(field.name, Nil) :: Nil
        }
      })
  println(validFieldPaths)

  val verb: P[Verb] =
    P(":" ~ AnyChar.rep.!)
      .map(Verb)

  val letter: P[String] =
    P(CharIn('A' to 'Z', 'a' to 'z')).!

  val decimalDigit: P[String] =
    P(CharIn('0' to '9')).!

  val ident: P[String] =
    P(letter ~ (letter | decimalDigit | "_".!).rep).!

  val fieldPath: P[FieldPath] =
    P(ident.!.rep(min = 1, sep = "."))
      .map({ case (head +: tail) => FieldPath(head, tail) })

  val wildcard: P[Segment.Wildcard.type] =
    P("*")
      .map(_ => Segment.Wildcard)

  val recursiveWildcard: P[Segment.RecursiveWildcard.type] =
    P("**")
      .map(_ => Segment.RecursiveWildcard)

  val literal: P[Segment.Literal] =
    P(CharPred(c => c != '*' && c != '/' && c != '{' && c != '}').rep.!)
      .map(Segment.Literal)

  val variable: Parser[Segment.Variable] =
    P("{" ~ fieldPath ~ ("=" ~ segments).? ~ "}")
      .map(Segment.Variable.tupled)

  val segment: P[Segment] =
    recursiveWildcard | wildcard | variable | literal

  val segments: P[Segments] =
    segment
      .rep(min = 1, sep = "/")
      .map({ case (head +: tail) => Segments(head, tail) })

  val template: P[Template] =
    P("/" ~ segments ~ verb.?)
      .map(Template.tupled)

}

object TemplateParsers {

  def parseTemplate[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    s: String
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Either[String, Template] =
    new TemplateParsers[DescriptorProto]().template.parse(s) match {
      case success: Parsed.Success[Template] => Right(success.value)
      case failure: Parsed.Failure           => Left(failure.msg)
    }

}
