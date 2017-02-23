package repositories

import models.Item
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class ItemRepository(implicit ec: ExecutionContext) {

  def fetchItem(id: Long): Future[Option[Item]] = id match {
    case 123L => Future.successful(Some(Item(name = "Bim", id = 123L)))
    case _ => Future.successful(None)
  }

}
