package base

import akka.actor.Actor

case class Info(name: String)

class BaseActor extends Actor {
  override def receive: Receive = {
    case Info(name) => println(name)
  }

  override def preStart(): Unit = {
    super.preStart()
  }
}
