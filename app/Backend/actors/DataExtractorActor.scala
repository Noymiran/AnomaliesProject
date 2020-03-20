package Backend.actors

import java.util.Calendar

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.stream.ActorMaterializer
import models.Task
import play.api.libs.json.Json
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import requests.{ HttpClient, PrometheusSeriesConverter }

object DataExtractorActor {

  case class ExtractData(task: Task)

}

class DataExtractorActor(detector: ActorRef) extends Actor with ActorLogging {

  import DataExtractorActor._

  import scala.concurrent.ExecutionContext.Implicits._

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case ExtractData(task) => {
      log.info(s"Extract data from task ${task.uuid} in time" + Calendar.getInstance().getTime())
      log.info(Json.prettyPrint(Json.toJson(task)))
      val prometheusSeriesConverter = new PrometheusSeriesConverter(task)
      task.dataResource match {
        case "Prometheus" => {
          val httpClient = new HttpClient(task)
          val wsClient = StandaloneAhcWSClient()
          prometheusSeriesConverter.buildQuery() match {
            case "NonValid" => log.error("Did not get a valid query")
            case query =>
              log.info(query)
              httpClient.call(wsClient, query)
                .andThen {
                  case _ => {
                    wsClient.close()
                  }
                }.onComplete(
                  _.map(prometheusSeriesConverter.buildTimeSeries(_).map(x =>
                    x.map(dataTimeSeries =>
                      detector ! dataTimeSeries)))
                )
          }
        }
      }

    }
  }
}
