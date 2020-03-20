package Backend

import Backend.actors._
import Backend.detectors.{ DetectorActor, EgadsSingleSeriesDetector }
import akka.actor.{ ActorSystem, Props }
import akka.routing.FromConfig
import services.{ AnomaliesTimeSeriesService, TasksService }

class Supervisor(tasksService: TasksService, anomaliesTimeSeriesService: AnomaliesTimeSeriesService)(implicit system: ActorSystem) {
  lazy val taskManagementActor = system.actorOf(Props(classOf[TasksManagementActor], tasksService, dataExtractorActor), "TaskManagerActor")
  lazy val dataExtractorActor = system.actorOf(Props(classOf[DataExtractorActor], detector), "DataExtractorActor")
  lazy val anomalyWriterActor = system.actorOf(FromConfig.props(Props(classOf[AnomalyWriterActor], tasksService)), "AnomalyWriterActor")
  lazy val detector = system.actorOf(DetectorActor(anomalyWriterActor, new EgadsSingleSeriesDetector(), anomaliesTimeSeriesService), "EgadsSingleSeriesDetector")
}