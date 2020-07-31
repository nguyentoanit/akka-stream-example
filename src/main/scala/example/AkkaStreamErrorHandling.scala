package example

import akka.actor.ActorSystem
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.ActorMaterializer
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl._

object AkkaStreamErrorHandling extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  // Handle with log
  Source(-5 to 5)
    .map(1 / _) //throwing ArithmeticException: / by zero
    .log("error logging")
    .runForeach(println)

  // Handle with recover
  Source(-5 to 5)
    .map(1 / _) //throwing ArithmeticException: / by zero
    .recover {
      case e: Exception => e.getMessage
    }
    .runForeach(println)

  // Handle with recoverWithRetries
  val planB = Source(List("five", "six", "seven", "eight"))
  Source(-5 to 5)
    .map(1 / _) //throwing ArithmeticException: / by zero
    .recoverWithRetries(1, {
      case e: Exception =>
        println(s"Exception: ${e.getMessage}")
        planB
    })
    .runForeach(println)

  // Handle with Supervision Strategy
  Source(-5 to 5)
    .map(1 / _) //throwing ArithmeticException: / by zero
    .map(1 + _)
    .map(_ - 1)
    .withAttributes(supervisionStrategy(resumingDecider))
    .runForeach(println)
}
