package routes

import akka.http.scaladsl.server.Directives._

class IndexRoutes(workingDirectory: String) {
    def getExtensions(fileName: String) : String = {
      val index = fileName.lastIndexOf('.')
      if(index != 0) {
        fileName.drop(index+1)
      } else ""
    }

    def routes = get {
      encodeResponse {
        getFromFile(s"$workingDirectory/index.html")
      }
    }

}
