package protoroutes.generator.httprule

import org.scalacheck.Gen
import scalapb.GeneratedMessageCompanion

object SegmentGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Segment] =
    Gen.sized(Sized[A])

  def literal[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Segment.Literal] =
    for {
      value <- Gen.alphaNumStr
    } yield Segment.Literal(value)

  def variable[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Segment.Variable] =
    for {
      fieldPath <- FieldPathGen[A]
      segments  <- Gen.option(SegmentsGen[A])
    } yield Segment.Variable(fieldPath, segments)

  object Sized {

    def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
      n: Int
    )(
      implicit A: GeneratedMessageCompanion[A]
    ): Gen[Segment] =
      if (n <= 0) {
        Gen.oneOf(
          Gen.const(Segment.Wildcard),
          SegmentGen.literal[A]
        )
      } else {
        Gen.oneOf(
          Gen.const(Segment.Wildcard),
          SegmentGen.literal[A],
          variable[A](n / 2)
        )
      }

    def variable[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
      n: Int
    )(
      implicit A: GeneratedMessageCompanion[A]
    ): Gen[Segment.Variable] =
      for {
        fieldPath <- FieldPathGen[A]
        segments  <- if (n <= 0) Gen.const(None) else Gen.option(SegmentsGen.Sized[A](n / 2))
      } yield Segment.Variable(fieldPath, segments)

  }

}
