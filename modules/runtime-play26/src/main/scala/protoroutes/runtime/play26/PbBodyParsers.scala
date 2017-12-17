package protoroutes.runtime.play26

import javax.inject.{Inject, Singleton}
import play.api.mvc.{BodyParser, PlayBodyParsers, Results}
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scalapb.GeneratedMessageCompanion
import scalapb.json4s.JsonFormat

@Singleton
class PbBodyParsers @Inject()(parse: PlayBodyParsers) {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit
    A: GeneratedMessageCompanion[A],
    ec: ExecutionContext
  ): BodyParser[A] =
    parse.using({ header =>
      PbFormat.detect(header.mediaType) match {
        case PbFormat.Binary => binary[A]
        case PbFormat.Json   => json[A]
      }
    })

  def binary[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit
    A: GeneratedMessageCompanion[A],
    ec: ExecutionContext
  ): BodyParser[A] =
    parse.byteString.validate({ bs =>
      try {
        Right(A.parseFrom(bs.iterator.asInputStream))
      } catch {
        case NonFatal(e) =>
          Left(
            Results.BadRequest(s"failed to parse PB binary: ${e.getMessage}")
          )
      }
    })

  def json[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit
    A: GeneratedMessageCompanion[A],
    ec: ExecutionContext
  ): BodyParser[A] =
    parse.tolerantText.validate({ text =>
      try {
        Right(JsonFormat.fromJsonString[A](text))
      } catch {
        case NonFatal(e) =>
          Left(Results.BadRequest(s"failed to parse PB JSON: ${e.getMessage}"))
      }
    })

}
