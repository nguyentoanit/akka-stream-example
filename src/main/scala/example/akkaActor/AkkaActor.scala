package example.akkaActor

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await

object AkkaActor {
  def main(args: Array[String]): Unit = {
    implicit val timeout = Timeout(5 seconds) // needed for `?` below
    val system = ActorSystem("TestActorSystem")
    val baseActor = system.actorOf(Props[BaseActor])
    baseActor ! Send("Hello!!!")
    val reponse = baseActor ? Question("How old are you?")
    val answer = Await.result(reponse, timeout.duration)
    println(reponse)
  }
}
