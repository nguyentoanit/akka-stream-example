package example.akkaStream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, DelayOverflowStrategy, KillSwitches}
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
