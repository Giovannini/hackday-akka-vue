package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import models.Item
import repositories.ItemRepository
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext

class JsonRoutes()(implicit ec: ExecutionContext) {

  val itemRepository: ItemRepository = new ItemRepository()
  implicit val itemFormat = jsonFormat2(models.Item)

  def routes = get {
    path(LongNumber) { id =>
      val maybeItem: Future[Option[Item]] = itemRepository.fetchItem(id)

      onSuccess(maybeItem) {
        case Some(item) => complete(item)
        case None => complete(StatusCodes.NotFound)
      }
    }
  }

}
