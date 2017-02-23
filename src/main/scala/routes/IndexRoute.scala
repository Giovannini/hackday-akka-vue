package routes

import akka.http.scaladsl.server.Directives._

class IndexRoutes() {
    def getExtensions(fileName: String) : String = {
      val index = fileName.lastIndexOf('.')
      if(index != 0) {
        fileName.drop(index+1)
      } else ""
    }

    val workingDirectory = System.getProperty("user.dir")

    def routes = get {
      encodeResponse {
        getFromFile(s"$workingDirectory/index.html")
      }
    }

}
