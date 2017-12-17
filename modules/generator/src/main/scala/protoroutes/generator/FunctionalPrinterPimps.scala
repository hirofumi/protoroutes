package protoroutes.generator

import scalapb.compiler.FunctionalPrinter
import scalapb.compiler.FunctionalPrinter.PrinterEndo

trait FunctionalPrinterPimps {

  implicit final class FunctionalPrinterSyntax(
    private val self: FunctionalPrinter
  ) {

    def indented(f: PrinterEndo): FunctionalPrinter =
      self.indent.call(f).outdent

  }

}

object FunctionalPrinterPimps extends FunctionalPrinterPimps
