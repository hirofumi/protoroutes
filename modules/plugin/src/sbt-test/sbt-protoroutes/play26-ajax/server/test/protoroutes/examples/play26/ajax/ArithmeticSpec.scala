package protoroutes.examples.play26.ajax

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

class ArithmeticSpec extends PlaySpec with GuiceOneServerPerSuite with OneBrowserPerSuite with HeadlessChromeFactory {

  "Scala.js client served by Play server" must {
    "communicate with the server" in {
      go to s"http://localhost:$port/"
      eventually {
        find(id("add-2-3")).get.text mustBe "5"
        find(id("mul-2-3")).get.text mustBe "6"
      }
    }
  }

}
