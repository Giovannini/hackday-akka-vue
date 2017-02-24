import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

import routes.HelloRoute
import routes.JsonRoutes
import routes.IndexRoutes
import routes.AssetsRoute
import routes.StreamingRoute
import routes.ShakespeareRoute

object Application {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val port: Int = 8080

    val workingDirectory = System.getProperty("user.dir")

    val route =
      path("hello") { new HelloRoute().routes } ~
      pathPrefix("json") { new JsonRoutes().routes } ~
      pathPrefix("assets") { new AssetsRoute(workingDirectory).routes } ~
      pathPrefix("streaming") { new StreamingRoute().routes } ~
      pathPrefix("theatre") { new ShakespeareRoute(workingDirectory).routes } ~
      new IndexRoutes(workingDirectory).routes

    val bindingFuture = Http().bindAndHandle(route, "localhost", port)

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
