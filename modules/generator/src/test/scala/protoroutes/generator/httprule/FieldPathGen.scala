package protoroutes.generator.httprule

import org.scalacheck.Gen
import scalapb.GeneratedMessageCompanion
import scalapb.descriptors.Descriptor

object FieldPathGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[FieldPath] =
    fromDescriptor(A.scalaDescriptor)

  def fromDescriptor(descriptor: Descriptor): Gen[FieldPath] =
    Gen.oneOf(
      descriptor.fields.map(_.name).map(FieldPath(_, Nil))
    )

}
