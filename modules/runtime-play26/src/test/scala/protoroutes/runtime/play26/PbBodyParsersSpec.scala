package protoroutes.runtime.play26

import akka.stream.Materializer
import akka.util.ByteString
import com.google.protobuf.wrappers.{
  BoolValue,
  DoubleValue,
  FloatValue,
  Int32Value,
  Int64Value,
  StringValue
}
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{Headers, PlayBodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.implicitConversions
import scalapb.GeneratedMessageCompanion
import scalapb.json4s.JsonFormat

class PbBodyParsersSpec extends PlaySpec with GuiceOneAppPerSuite {

  val `application/json`: String       = "application/json"
  val `application/x-protobuf`: String = "application/x-protobuf"

  def asByteString[A <: scalapb.GeneratedMessage](a: A): ByteString =
    ByteString(a.toByteArray)

  def asJsonByteString[A <: scalapb.GeneratedMessage](a: A): ByteString =
    ByteString(JsonFormat.toJsonString(a))

  implicit val materializer: Materializer =
    app.materializer

  import materializer.executionContext

  val parsers: PbBodyParsers =
    new PbBodyParsers(PlayBodyParsers())

  def parse[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    body: ByteString,
    contentType: Option[String]
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Either[Result, A] = {
    val headers =
      contentType match {
        case Some(ct) => Headers("Content-Type" -> ct)
        case None     => Headers()
      }
    Await.result(
      parsers[A].apply(FakeRequest().withHeaders(headers)).run(body),
      Duration.Inf
    )
  }

  def parseBinary[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    bytes: ByteString
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Either[Result, A] =
    parse[A](bytes, Some(`application/x-protobuf`))

  def parseJson[A <: scalapb.GeneratedMessage with scalapb.Message[A]](
    json: String
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Either[Result, A] =
    parse[A](ByteString(json), Some(`application/json`))

  def testRoundTripViaBinary[
    A <: scalapb.GeneratedMessage with scalapb.Message[A]
  ](
    a: A,
    contentType: Option[String] = Some(`application/x-protobuf`)
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Assertion =
    parseBinary[A](asByteString(a)) mustBe Right(a)

  def testRoundTripViaJson[
    A <: scalapb.GeneratedMessage with scalapb.Message[A]
  ](
    a: A,
    contentType: Option[String] = Some(`application/json`)
  )(
    implicit A: GeneratedMessageCompanion[A]
  ): Assertion =
    parseJson[A](JsonFormat.toJsonString(a)) mustBe Right(a)

  "invalid binary results in 400" in {
    val parsed = parseBinary[BoolValue](ByteString(42))
    parsed.left.map(_.header.status) mustBe Left(BAD_REQUEST)
  }

  "invalid JSON results in 400" in {
    val parsed = parseJson[BoolValue]("42")
    parsed.left.map(_.header.status) mustBe Left(BAD_REQUEST)
  }

  "parse as binary when Content-Type header represents binary" in {
    testRoundTripViaBinary(Int32Value(42), Some(`application/x-protobuf`))
  }

  "parse as binary when Content-Type does not exist" in {
    testRoundTripViaBinary(Int32Value(42), None)
  }

  "parse as JSON when Content-Type header represents JSON" in {
    testRoundTripViaJson(Int32Value(42), Some(`application/json`))
    testRoundTripViaJson(Int32Value(42), Some("text/json"))
  }

  "parse wrapped scalar values as JSON" in {
    parseJson[BoolValue]("false") mustBe Right(BoolValue(false))
    parseJson[BoolValue]("true") mustBe Right(BoolValue(true))
    parseJson[DoubleValue]("4.2") mustBe Right(DoubleValue(4.2))
    parseJson[FloatValue]("4.2") mustBe Right(FloatValue(4.2f))
    parseJson[Int32Value]("42") mustBe Right(Int32Value(42))
    parseJson[Int64Value]("42") mustBe Right(Int64Value(42L))
    parseJson[StringValue]("\"Lorem\"") mustBe Right(StringValue("Lorem"))
  }

  "round trip via JSON" in {
    testRoundTripViaJson(Int32Value(42))
    testRoundTripViaJson(StringValue("Lorem ipsum"))
  }

  "round trip via binary" in {
    testRoundTripViaBinary(Int32Value(42))
    testRoundTripViaBinary(StringValue("Lorem ipsum"))
  }

}
