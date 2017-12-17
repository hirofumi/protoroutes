package protoroutes.examples

import com.google.protobuf.wrappers.Int32Value
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers._

class ArithmeticSpec extends PlaySpec with GuiceOneServerPerSuite {

  private[this] def request(subPath: String, contentType: String): WSRequest =
    app.injector
      .instanceOf[WSClient]
      .url(s"http://localhost:$port/api/arithmetic/$subPath")
      .addHttpHeaders("Content-Type" -> contentType)

  private[this] def requestBinary(subPath: String): WSRequest =
    request(subPath, "application/x-protobuf")

  private[this] def requestJson(subPath: String): WSRequest =
    request(subPath, "application/json")

  "add" must {
    "return 200 for valid binary POST" in {
      val response = await(requestBinary("add").post(Int32Pair(2, 3).toByteArray))
      response.status mustBe OK
      Int32Value.parseFrom(response.bodyAsBytes.toArray) mustBe Int32Value(5)
    }
    "return 200 for valid JSON POST" in {
      val response = await(requestJson("add").post("""{"x":2,"y":3}"""))
      response.status mustBe OK
      response.body mustBe "5"
    }
    "return 400 for invalid binary POST" in {
      val response = await(requestBinary("add").post(Int32Pair(2, 3).toByteArray.take(3)))
      response.status mustBe BAD_REQUEST
    }
    "return 400 for invalid JSON POST" in {
      val response = await(requestJson("add").post("""{"x":2,"y":}"""))
      response.status mustBe BAD_REQUEST
    }
    "return 404 for PUT" in {
      val response = await(requestBinary("add").put(Int32Pair(2, 3).toByteArray))
      response.status mustBe NOT_FOUND
    }
  }

  "mul" must {
    "return 200 for valid binary PUT" in {
      val response = await(requestBinary("mul").put(Int32Pair(2, 3).toByteArray))
      response.status mustBe OK
      Int32Value.parseFrom(response.bodyAsBytes.toArray) mustBe Int32Value(6)
    }
    "return 200 for valid JSON PUT" in {
      val response = await(requestJson("mul").put("""{"x":2,"y":3}"""))
      response.status mustBe OK
      response.body mustBe "6"
    }
    "return 404 for POST" in {
      val response = await(requestBinary("mul").post(Int32Pair(2, 3).toByteArray))
      response.status mustBe NOT_FOUND
    }
  }

}
