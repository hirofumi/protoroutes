package protoroutes.generator.httprule

import org.scalacheck.Gen
import scalapb.GeneratedMessageCompanion

object FieldPathGen {

  def apply[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    implicit A: GeneratedMessageCompanion[A]
  ): Gen[FieldPath] = {

//    A.defaultInstance.toPMessage.value

//    println(A.nestedMessagesCompanions.map(_.scalaDescriptor.nestedMessages))
//    println( A.scalaDescriptor.fields.map(_.asProto .`type`.map(_.isTypeMessage)))

    Gen.oneOf(
      A.scalaDescriptor.fields
        .map({ field =>
          FieldPath(field.name, Nil)
        })
    )

  }

}
