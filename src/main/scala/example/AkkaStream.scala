package example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

final case class Author(handle: String)
final case class Tweet(author: Author, timestamp: Long, body: String)

object AkkaStream extends App{
  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map((tweet) ⇒ {
    tweet.author.handle + tweet.body + tweet.timestamp.toString
  })
  val result =  tweets.via(count)
  result.runForeach( (param) => {
    println(param)
  })
}
