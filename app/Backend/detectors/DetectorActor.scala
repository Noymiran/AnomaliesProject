package Backend.detectors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.routing.FromConfig
import models.{ AnomaliesTimeSeries, DataTimeSeries }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.AnomaliesTimeSeriesService

class DetectorActor(outputActor: ActorRef, detector: Detector, anomaliesTimeSeriesService: AnomaliesTimeSeriesService) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ts: DataTimeSeries =>
      val resultAnomalies = detector.detect(ts)

      resultAnomalies map { result =>
        val anomalies = AnomaliesTimeSeries(None, ts.taskID, result.anomalies, ts.dimensions)
        anomaliesTimeSeriesService.create(anomalies).map {
          case Right(id) =>
            log.info("Created anomalies time series " + id.toString + " in DB")
            outputActor ! result
          case Left(err) => log.error("Didn't created anomalies time series: " + err)
        }.recover { case err => log.error(err.toString) }
      }
  }
}

object DetectorActor {
  def apply(outputActor: ActorRef, detector: Detector, anomaliesTimeSeriesService: AnomaliesTimeSeriesService): Props =
    FromConfig.props(Props(classOf[DetectorActor], outputActor, detector, anomaliesTimeSeriesService))
}

