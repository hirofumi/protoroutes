package protoroutes.generator.httprule

import com.google.protobuf.descriptor.DescriptorProto
import org.scalacheck.Arbitrary
import org.scalatest.{DiagrammedAssertions, FunSuite}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class TemplateSuite
  extends FunSuite with DiagrammedAssertions
  with GeneratorDrivenPropertyChecks {

  test("parsed .text must be equal to the original") {
    implicit val arbitraryTemplate: Arbitrary[Template] =
      TemplateGen.arbitrary[DescriptorProto]
    forAll("template") { template: Template =>
      val parsed = Template.parse(template.text)
      assert(parsed === Right(template))
    }
  }

}
