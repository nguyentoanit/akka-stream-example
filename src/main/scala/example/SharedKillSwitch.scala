package example

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.concurrent.Await
import scala.concurrent.duration._

object SharedKillSwitch {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {
    val countingSrc = Source(Stream.from(1)).delay(1.second, DelayOverflowStrategy.backpressure)
    val lastSnk = Sink.foreach[Int](println)
    val sharedKillSwitch = KillSwitches.shared("my-kill-switch")
    if (args.length > 0) sharedKillSwitch.shutdown()
      val delayedLast = countingSrc
        .delay(1.second, DelayOverflowStrategy.backpressure)
        .via(sharedKillSwitch.flow)
        .runWith(lastSnk)


  }
}
