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

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
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


  // Define Flow
  val count: Flow[Tweet, String, NotUsed] = Flow[Tweet].map(tweet ⇒ {
    tweet.author.handle + ": " + tweet.body + " " + tweet.timestamp.toString
  })

  // Combine Source and Flow
  val combineSource: Source[String, NotUsed] = tweets.via(count)

  // Define Sink
  val consoleSink: Sink[String, Future[Done]] = Sink.foreach[String](println)

  combineSource.runWith(consoleSink)

  // Broadcast
  val g1 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 10)
    val out = Sink.foreach[Int](println)

    val bcast = builder.add(Broadcast[Int](4))

    val f1 = Flow[Int].map(_ + 10)
    val f2 = Flow[Int].map(_ - 10)
    val f3 = Flow[Int].map(_ * 10)
    val f4 = Flow[Int].map(_ / 10)

    in ~> bcast
    bcast ~> f1 ~> out
    bcast ~> f2 ~> out
    bcast ~> f3 ~> out
    bcast ~> f4 ~> out
    ClosedShape
  })
  g1.run

  // Balance and Broadcast
  val g2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val in = Source(1 to 10)
    val out = Sink.foreach[Any](println)

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))
    val balance = builder.add(Balance[Int](2))

    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

    in ~> balance ~> f1 ~> merge ~> out
          balance ~> f2 ~> merge
    ClosedShape
  })
  g2.run

}
