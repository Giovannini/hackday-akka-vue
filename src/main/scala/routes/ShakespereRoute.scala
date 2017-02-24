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
import spray.json.DefaultJsonProtocol._
import models.Character
import repositories.ShakespeareRepository
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import spray.json._

class ShakespeareRoute(workingDirectory: String)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {

  val repository = new ShakespeareRepository()

  val source: Source[Character, NotUsed] = Source(repository.romeoEtJuliette)
  val romeoEtJulietteIterable = repository.romeoEtJuliette.toIterator

  def toNewLineFlow[A]: Flow[A, String, NotUsed] = Flow[A].map(_.toString + "\n")

  implicit val characterFormat = jsonFormat2(Character)

  implicit val toResponseMarshaller: ToResponseMarshaller[Source[String, Any]] =
    Marshaller.opaque { items =>
      val data = items.map(item => ChunkStreamPart(item))
      HttpResponse(entity = HttpEntity.Chunked(MediaTypes.`application/json`, data))
    }

  def throttler[A](duration: FiniteDuration): Flow[A, A, NotUsed] =
    Flow[A].throttle(1, duration, 1, ThrottleMode.shaping)

  def routes = path("romeoEtJuliette") {
    handleWebSocketMessages(Flow[Message].mapConcat {
      case tm: TextMessage if romeoEtJulietteIterable.hasNext =>
        TextMessage(romeoEtJulietteIterable.next().toJson.toString) :: Nil
      case other =>
        println(other)
        Nil
    })
  } ~
    path("romeoAndJuliette") { get(complete(romeoAndJulietteSource)) } ~
    pathEnd { getFromFile(s"$workingDirectory/theatre.html") }

  private def romeoAndJulietteSource: Source[String, NotUsed] = source
    .map(c => s"${c.name} - ${c.text}")
    .via(toNewLineFlow)
    .via(throttler(0.5.second))

}
