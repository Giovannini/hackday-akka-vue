package routes

import akka.http.scaladsl.server.Directives._

class AssetsRoute() {

  val workingDirectory = System.getProperty("user.dir")

  def routes = get {
    pathPrefix("(.+/?)*".r) { asset =>
      encodeResponse {
        getFromFile(s"$workingDirectory/client/$asset")
      }
    }
  }
}
