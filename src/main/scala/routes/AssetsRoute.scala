package routes

import akka.http.scaladsl.server.Directives._

class AssetsRoute(workingDirectory: String) {

  def routes = get {
    path(Remaining) { asset =>
      println(asset)
      encodeResponse {
        getFromFile(s"$workingDirectory/client/$asset")
      }
    }
  }
}
