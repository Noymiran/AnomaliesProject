package controllers

import java.util.UUID

import akka.actor.ActorSystem
import javax.inject.Inject
import models.{ AnomaliesTimeSeries, Task }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.{ AnomaliesTimeSeriesService, TasksService }
import utils.Logging

class AnomaliesController @Inject() (tasksService: TasksService, anomaliesTimeSeriesService: AnomaliesTimeSeriesService, mongoTaskController: MongoCRUDController[Task, UUID], mongoAnomaliesController: MongoCRUDController[AnomaliesTimeSeries, UUID])(redirectUrl: UUID => Call)(implicit system: ActorSystem)
    extends Controller with Logging {

  val limit = mongoAnomaliesController.DEFAULT_LIMIT.head.toInt

  def getAll: Action[AnyContent] = Action.async {
    anomaliesTimeSeriesService.search(Json.obj(), limit)
      .map(
        entity => Ok(
          Json.toJson(entity)
        )
      )
  }

  def read(id: UUID): Action[AnyContent] = {
    mongoAnomaliesController.read(id)
  }

  def delete(id: UUID): Action[AnyContent] = {
    mongoAnomaliesController.delete(id)
  }
}