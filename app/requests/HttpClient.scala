package requests

import com.typesafe.config.ConfigFactory
import models.Task
import play.api.libs.ws._
import utils.Logging

import scala.concurrent.Future

abstract class GenericHttpClient[T](task: Task) {
  def call(wsClient: StandaloneWSClient, query: String): Future[T]
}

class HttpClient(task: Task) extends GenericHttpClient[String](task) with Logging {

  import DefaultBodyReadables._

  import scala.concurrent.ExecutionContext.Implicits._

  lazy val config = ConfigFactory.load

  def call(wsClient: StandaloneWSClient, query: String): Future[String] = {
    val dataResource = task.dataResource
    val hostname: String = config getString (s"$dataResource.hostname")
    val port: Int = config getInt (s"$dataResource.port")
    val apiPathUrl: String = config getString (s"$dataResource.apipath")
    val urlQuery = new String(s"http://$hostname:$port$apiPathUrl?query=$query")
    wsClient.url(urlQuery).get().map { response ⇒
      val statusText: String = response.statusText
      log.info("Got status code " + statusText)
      val body = response.body[String]
      body
    }
  }
}
