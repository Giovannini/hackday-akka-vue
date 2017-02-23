package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import java.nio.file.{Files, Paths}

class IndexRoutes() {
    def getExtensions(fileName: String) : String = {
      val index = fileName.lastIndexOf('.')
      if(index != 0) {
        fileName.drop(index+1)
      } else ""
    }

    val workingDirectory = System.getProperty("user.dir")
    val path = Paths.get(workingDirectory + "/index.html")
    val fullPath = if (Files.exists(path)) path else Paths.get("")
    val ext = getExtensions(fullPath.getFileName.toString)
    val c : ContentType = ContentType(MediaTypes.forExtensionOption(ext).getOrElse(MediaTypes.`text/plain`), () => HttpCharsets.`UTF-8`)
    val byteArray = Files.readAllBytes(fullPath)
    val response = HttpResponse(akka.http.scaladsl.model.StatusCodes.OK, entity = HttpEntity(c, byteArray))

    def routes = get {
      complete(response)
    }
}
