package protoroutes.examples

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
final class GreetingImpl() extends GreetingGrpc.Greeting {

  override def hello(request: HelloRequest): Future[HelloResponse] =
    Future.successful(HelloResponse(s"hello, ${request.name}"))

}
