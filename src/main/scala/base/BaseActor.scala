package base

import akka.actor.Actor

case class Send(message: String)

case class Question(message: String)

class BaseActor extends Actor {
  override def receive: Receive = {
    case Send(message) => println(message)
    case Question(message) => {
      println(message)
      println("Thinking...")

      Thread.sleep(4000)
      sender() ! "11"
    }
  }
}
