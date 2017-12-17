package protoroutes.examples

import com.google.protobuf.wrappers.Int32Value
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class ArithmeticSpec extends PlaySpec with GuiceOneServerPerSuite {

  "add" in {
    val client = app.injector.instanceOf[WSClient]
    val endpoint = s"http://localhost:$port/api/arithmetic/add"
    val response = await(client.url(endpoint).post(Int32Pair(2, 3).toByteArray))
    response.status mustBe OK
    Int32Value.parseFrom(response.bodyAsBytes.toArray) mustBe Int32Value(5)
  }

}
