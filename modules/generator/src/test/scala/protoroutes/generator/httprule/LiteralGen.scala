package protoroutes.generator.httprule

import org.scalacheck.Gen
import scalapb.GeneratedMessageCompanion

object LiteralGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Segment.Literal] =
    for {
      value <- Gen.alphaNumStr
    } yield Segment.Literal(value)

}
