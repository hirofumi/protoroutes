package protoroutes.runtime.play26

import akka.util.ByteString
import play.api.http.{MediaType, Writeable}
import play.api.mvc.Request
import scalapb.GeneratedMessageCompanion
import scalapb.json4s.JsonFormat

sealed abstract class PbFormat {

  def contentType: String

  def toWriteable[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Writeable[A]

}

object PbFormat {

  implicit def requestToWriteable[
    A <: scalapb.GeneratedMessage with scalapb.Message[A]
  ](
    implicit
    request: Request[_],
    A: GeneratedMessageCompanion[A]
  ): Writeable[A] =
    detect(request.mediaType).toWriteable

  def detect(mediaType: Option[MediaType]): PbFormat =
    if (mediaType.exists(_.mediaSubType.equalsIgnoreCase("json"))) {
      Json
    } else {
      Binary
    }

  case object Binary extends PbFormat {

    def contentType: String =
      "application/x-protobuf"

    def toWriteable[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
      implicit A: GeneratedMessageCompanion[A]
    ): Writeable[A] =
      Writeable(a => ByteString(A.toByteArray(a)), Some(contentType))

  }

  case object Json extends PbFormat {

    def contentType: String =
      "application/json"

    def toWriteable[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
      implicit A: GeneratedMessageCompanion[A]
    ): Writeable[A] =
      Writeable(a => ByteString(JsonFormat.toJsonString(a)), Some(contentType))

  }

}
