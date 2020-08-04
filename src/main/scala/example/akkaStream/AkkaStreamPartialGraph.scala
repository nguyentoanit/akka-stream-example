package example.akkaStream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source, Unzip, Zip}

object AkkaStreamPartialGraph extends App {
  implicit val system: ActorSystem = ActorSystem("Demo-Basics")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import GraphDSL.Implicits._

  val numbers: Source[Int, NotUsed] = Source(1 to 10)
  val pairs = Source.fromGraph(GraphDSL.create() { implicit builder =>

    val zip = builder.add(Zip[Int, Int]())

    numbers.filter(_ % 2 != 0) ~> zip.in0
    numbers.filter(_ % 2 == 0) ~> zip.in1
    SourceShape(zip.out)
  })

  val pairUpWithToString =
    Flow.fromGraph(GraphDSL.create() { implicit b =>

      val broadcast = b.add(Broadcast[Int](2))
      val zip = b.add(Zip[Int, String]())

      broadcast.out(0).map(identity) ~> zip.in0
      broadcast.out(1).map(_.toString) ~> zip.in1

      FlowShape(broadcast.in, zip.out)
    })

  val extractOne = Sink.fromGraph(GraphDSL.create() { implicit builder =>

    val unzip = builder.add(Unzip[Int, Int])
    unzip.out1 ~> Sink.ignore
    unzip.out0 ~> Sink.foreach(println)
    SinkShape(unzip.in)
  })

  val runnable = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    pairs ~> extractOne
    ClosedShape
  })

  runnable.run(materializer)
}
