package example

import akka.actor.{ActorSystem, Props}
import base.{BaseActor, Info}

object AkkaActor {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("TestActorSystem")
    val baseActor = system.actorOf(Props[BaseActor])
    baseActor ! Info("Hello Actor!!!")
  }
}
