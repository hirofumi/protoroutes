package protoroutes.examples

import com.google.protobuf.wrappers.Int32Value
import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
final class ArithmeticImpl() extends ArithmeticGrpc.Arithmetic {

  override def add(pair: Int32Pair): Future[Int32Value] =
    Future.successful(Int32Value(pair.x + pair.y))

  override def mul(pair: Int32Pair): Future[Int32Value] =
    Future.successful(Int32Value(pair.x * pair.y))

}
