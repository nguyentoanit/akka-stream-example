package example.akkaStream

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, DelayOverflowStrategy}

object AkkaStreamCompletionTimeout extends App {
  implicit val system: ActorSystem = ActorSystem("Demo-Basics")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  Source(1 to 10)
    .completionTimeout(5.seconds)
    .delay(1.second, DelayOverflowStrategy.backpressure)
    .recover {
      case e: Exception => e.getMessage // The stream has not been completed in 5 seconds.
    }
    .runForeach(println)
}
