package example.akkaStream

import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, _}
import akka.{Done, NotUsed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
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
  val in = Source(1 to 10)
  val out = Sink.seq[Int]
  // Broadcast
  val g1 = RunnableGraph.fromGraph(GraphDSL.create(out) { implicit builder =>
    out =>

    val bcast = builder.add(Broadcast[Int](4))
    val merge = builder.add(Merge[Int](4))

    val f1 = Flow[Int].map(_ + 10)
    val f2 = Flow[Int].map(_ - 10)
    val f3 = Flow[Int].map(_ * 10)
    val f4 = Flow[Int].map(_ / 10)

    in ~> bcast
    bcast ~> f1 ~> merge ~> out
    bcast ~> f2 ~> merge
    bcast ~> f3 ~> merge
    bcast ~> f4 ~> merge
    ClosedShape
  }).run()

  g1.onComplete {
    case Success(value) => println(value)
    case Failure(e) => println(e.getMessage)
  }

  // Balance, Broadcast and Merge
  val g2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val in = Source(1 to 10)
    val out = Sink.foreach[Any](println)

    val bcast1 = builder.add(Broadcast[Int](2))
    val bcast2 = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](4))
    val balancer = builder.add(Balance[Int](2))

    val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

    // Limit rate
    //    import scala.concurrent.duration.DurationInt
//    val f1, f2, f3, f4 = Flow[Int].map(_ + 10).throttle(1, 3.second)

    in ~> balancer ~> bcast1 ~> f1 ~> merge ~> out
                      bcast1 ~> f2 ~> merge
          balancer ~> bcast2 ~> f3 ~> merge
                      bcast2 ~> f4 ~> merge
    ClosedShape
  })
  g2.run
}
