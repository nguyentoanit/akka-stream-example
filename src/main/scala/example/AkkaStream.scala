package example

import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.Future
import akka.stream._
final case class Author(handle: String)
final case class Tweet(author: Author, timestamp: Long, body: String)

object AkkaStream extends App {

  // Create Source
  val tweets: Source[Tweet, NotUsed] = Source(Tweet(Author("rolandkuhn"),
                                                    System.currentTimeMillis,
                                                    "#akka rocks!") ::
    Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
    Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
    Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
    Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
    Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
    Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
    Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
    Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
    Tweet(Author("drama"),
          System.currentTimeMillis,
          "we compared #apples to #oranges!") ::
    Nil)

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  // Define Flow
  val count: Flow[Tweet, String, NotUsed] = Flow[Tweet].map(tweet ⇒ {
    tweet.author.handle + ": " + tweet.body + " " + tweet.timestamp.toString
  })

  // Combine Source and Flow
  val combineSource: Source[String, NotUsed] = tweets.via(count)

  // Define Sink
  val consoleSink: Sink[String, Future[Done]] = Sink.foreach[String](println)

  combineSource.runWith(consoleSink)

  // Balance and Broadcast
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 10)
    val out = Sink.foreach[Int](println)

    val bcast = builder.add(Broadcast[Int](4))

    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

    in ~> bcast ~> f1 ~> out
    bcast ~> f2 ~> out
    bcast ~> f3 ~> out
    bcast ~> f4 ~> out
    ClosedShape
  })
  g.run()

}
