package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class HelloRoute() {

  def routes = get {
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Let's go now!</h1>"))
  }

}
