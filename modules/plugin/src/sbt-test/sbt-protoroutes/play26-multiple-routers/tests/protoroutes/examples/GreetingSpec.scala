package protoroutes.examples

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class GreetingSpec extends PlaySpec with GuiceOneServerPerSuite {

  "hello" in {
    val client = app.injector.instanceOf[WSClient]
    val endpoint = s"http://localhost:$port/api/greeting/hello"
    val response = await(client.url(endpoint).post(HelloRequest("john").toByteArray))
    response.status mustBe OK
    HelloResponse.parseFrom(response.bodyAsBytes.toArray) mustBe HelloResponse("hello, john")
  }

}
