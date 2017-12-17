package protoroutes.runtime.ajax

import com.google.protobuf.CodedInputStream
import java.nio.ByteBuffer
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.typedarray.{ArrayBuffer, ArrayBufferInputStream}
import scalapb.GeneratedMessageCompanion

final case class PbAjax[
  A <: scalapb.GeneratedMessage,
  B <: scalapb.GeneratedMessage with scalapb.Message[B]
](
  private val method: String,
  private val url: String,
  timeout: Int,
  headers: Map[String, String],
  withCredentials: Boolean
)(
  implicit B: GeneratedMessageCompanion[B],
  executionContext: ExecutionContext
) extends (A => Future[B]) {

  def apply(message: A): Future[B] =
    Ajax(
      method          = method,
      url             = url,
      data            = ByteBuffer.wrap(message.toByteArray),
      timeout         = timeout,
      headers         = headers,
      withCredentials = withCredentials,
      responseType    = "arraybuffer"
    ).map(parseResponse)

  private[this] def parseResponse(xhr: XMLHttpRequest): B = {
    val response = xhr.response.asInstanceOf[ArrayBuffer]
    val stream   = new ArrayBufferInputStream(response)
    B.parseFrom(CodedInputStream.newInstance(stream))
  }

}
