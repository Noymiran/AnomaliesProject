package controllers

import java.util.UUID

import Backend.Supervisor
import Backend.actors.TasksManagementActor._
import akka.actor.ActorSystem
import javax.inject.Inject
import models.Task
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.{ AnomaliesTimeSeriesService, TasksService }
import utils.Logging

class TasksController @Inject() (tasksService: TasksService, anomaliesTimeSeriesService: AnomaliesTimeSeriesService, mongoController: MongoCRUDController[Task, UUID])(redirectUrl: UUID => Call)(implicit system: ActorSystem)
    extends Controller with Logging {

  lazy val supervisorActor = new Supervisor(tasksService, anomaliesTimeSeriesService)

  def search = Action.async(parse.json) { implicit request =>

    val limit = request.queryString.get("limit").getOrElse(mongoController.DEFAULT_LIMIT).head.toInt

    mongoController.validateAndThen[services.SearchQuery] {
      query =>
        tasksService
          .search(query, limit)
          .map(
            entity => Ok(
              Json.toJson(entity)
            )
          )
    }
  }

  def create = Action.async(parse.json) { implicit request =>
    mongoController.validateAndThen[Task] {
      entity =>
        tasksService.create(entity).map {
          case Right(id) => {
            supervisorActor.taskManagementActor ! CreateTaskActor(id)
            Created.withHeaders(LOCATION -> redirectUrl(id).url)
          }
          case Left(err) => BadRequest(err)
        }
    }
  }

  def update(id: UUID) = Action.async(parse.json) { implicit request =>
    mongoController.validateAndThen[Task] {
      entity =>
        tasksService.update(id, entity).map {
          case Right(id) => {
            supervisorActor.taskManagementActor ! UpdateTaskActor(id)
            Ok.withHeaders(LOCATION -> redirectUrl(id).url)
          }
          case Left(err) => BadRequest(err)
        }
    }
  }

  def delete(id: UUID) = Action.async {
    tasksService.delete(id).map {
      case Right(id) => {
        supervisorActor.taskManagementActor ! DeleteTaskActor(id)
        Ok
      }
      case Left(err) => BadRequest(err)
    }
  }

  def read(id: UUID) = {
    mongoController.read(id)
  }
}