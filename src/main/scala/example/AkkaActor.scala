package example

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import base.{BaseActor, Question, Send}

import scala.concurrent.Await
import scala.concurrent.duration._

object AkkaActor {
  def main(args: Array[String]): Unit = {
    implicit val timeout = Timeout(5 seconds) // needed for `?` below

    val system = ActorSystem("TestActorSystem")
    val baseActor = system.actorOf(Props[BaseActor])
    baseActor ! Send("Hello!!!")
    val reponse = baseActor ask Question("How old are you?")
    val answer = Await.result(reponse, timeout.duration)
    println(answer)
  }
}
