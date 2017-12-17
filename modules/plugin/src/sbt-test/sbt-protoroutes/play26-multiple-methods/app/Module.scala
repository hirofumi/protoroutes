import com.google.inject.AbstractModule
import protoroutes.examples.{ArithmeticGrpc, ArithmeticImpl}

class Module extends AbstractModule {

  def configure(): Unit = {
    bind(classOf[ArithmeticGrpc.Arithmetic]).to(classOf[ArithmeticImpl])
  }

}
