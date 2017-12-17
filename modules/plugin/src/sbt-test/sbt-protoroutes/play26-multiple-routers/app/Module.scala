import com.google.inject.AbstractModule
import protoroutes.examples.{
  ArithmeticGrpc,
  ArithmeticImpl,
  GreetingGrpc,
  GreetingImpl
}

class Module extends AbstractModule {

  def configure(): Unit = {
    bind(classOf[ArithmeticGrpc.Arithmetic]).to(classOf[ArithmeticImpl])
    bind(classOf[GreetingGrpc.Greeting]).to(classOf[GreetingImpl])
  }

}
