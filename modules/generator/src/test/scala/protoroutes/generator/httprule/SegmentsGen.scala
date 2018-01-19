package protoroutes.generator.httprule

import org.scalacheck.Gen
import scalapb.GeneratedMessageCompanion

object SegmentsGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[Segments] =
    Gen.sized(Sized[A])

  object Sized {

    def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
      n: Int
    )(
      implicit A: GeneratedMessageCompanion[A]
    ): Gen[Segments] =
      for {
        head <- SegmentGen.Sized[A](n / 2)
        tail <- Gen.listOf(SegmentGen.Sized[A](n / 2)).map(_.take(n - 1))
      } yield Segments(head, tail)

  }

}
