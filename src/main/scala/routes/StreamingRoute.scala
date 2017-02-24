package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream._
import akka.stream.scaladsl._
import akka.{ NotUsed }
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart

class StreamingRoute()(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {

  val source: Source[Int, NotUsed] = Source(1 to 100)

  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  def toNewLineFlow[A]: Flow[A, String, NotUsed] = Flow[A].map(_.toString + "\n")

  implicit val toResponseMarshaller: ToResponseMarshaller[Source[String, Any]] =
    Marshaller.opaque { items =>
      val data = items.map(item => ChunkStreamPart(item))
      HttpResponse(entity = HttpEntity.Chunked(MediaTypes.`application/json`, data))
    }

  def throttler[A](duration: FiniteDuration): Flow[A, A, NotUsed] =
    Flow[A].throttle(1, duration, 1, ThrottleMode.shaping)

  def routes =
    path("factorials") { complete(factorialsSource) } ~
    get {
      path(IntNumber) { i => complete(intSource(toMilliseconds(i))) } ~
      pathEnd { complete(intSource(1.second)) }
    }

  private def toMilliseconds(i: Int): FiniteDuration = if (i > 0) i.milliseconds else 1.second

  private def factorialsSource: Source[String, NotUsed] = source
    .scan(BigInt(1))((acc, next) => acc * next)
    .via(toNewLineFlow)
    .throttle(1, 0.5.second, 1, ThrottleMode.shaping)

  private def intSource(duration: FiniteDuration): Source[String, NotUsed] = source
    .via(toNewLineFlow)
    .via(throttler(duration))

}
