package protoroutes.generator.httprule

import org.scalacheck.{Arbitrary, Gen}
import scalapb.GeneratedMessageCompanion

object TemplateGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Template] =
    for {
      segments <- SegmentsGen[A]
    } yield Template(segments, None)

  def arbitrary[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Arbitrary[Template] =
    Arbitrary(TemplateGen[A])

}
