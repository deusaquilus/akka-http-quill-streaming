package io.test

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import monix.execution.Scheduler
import org.reactivestreams.Publisher

case class Person(name:String, age:Int)

object PersonDao {
  import io.getquill._

  implicit val scheduler = Scheduler.io()

  val ctx = new PostgresMonixJdbcContext(Literal, "postgresDB")
  import ctx._

  def byName(name:String): Publisher[Person] = {
    stream(query[Person].filter(_.name == "Joe")).toReactivePublisher
  }
}

object AkkaHttpTestReactive {

  def main(args:Array[String]): Unit = {
    println("Starting")

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          val peoplePub = PersonDao.byName("Joe")

          val start = Source.single("<h1>People:</h1>").map(ChunkStreamPart(_))
          val continue =
            Source
              .fromPublisher(peoplePub)
              .map(p => s"<h2>Say hello to ${p.name}</h2>")
              .intersperse("\n")
              .map(ChunkStreamPart(_))
          val output = start.concat(continue)

          complete(
            HttpResponse(entity = HttpEntity.Chunked(MediaTypes.`text/html`.toContentType(HttpCharsets.`UTF-8`), output))
          )
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  }
}
