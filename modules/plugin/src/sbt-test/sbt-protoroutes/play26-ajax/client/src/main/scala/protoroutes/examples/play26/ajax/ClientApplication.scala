package protoroutes.examples.play26.ajax

import org.scalajs.dom.document
import scalajs.concurrent.JSExecutionContext.Implicits.queue

object ClientApplication {

  private[this] val arithmeticAjax: ArithmeticAjax =
    ArithmeticAjax(headers = Map("Csrf-Token" -> "nocheck"))

  def main(args: Array[String]): Unit = {
    for (v <- arithmeticAjax.add(Int32Pair(2, 3))) {
      document.getElementById("add-2-3").innerHTML = v.value.toString
    }
    for (v <- arithmeticAjax.mul(Int32Pair(2, 3))) {
      document.getElementById("mul-2-3").innerHTML = v.value.toString
    }
  }

}
