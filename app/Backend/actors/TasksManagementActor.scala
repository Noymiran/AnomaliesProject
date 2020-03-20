package Backend.actors

import java.util.UUID

import Backend.actors.TaskActor._
import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill, Props }
import services.TasksService

object TasksManagementActor {

  final case class CreateTaskActor(id: UUID)

  final case class UpdateTaskActor(id: UUID)

  final case class DeleteTaskActor(id: UUID)

}

class TasksManagementActor(tasksService: TasksService, dataExtractorActor: ActorRef) extends Actor with ActorLogging {

  import TasksManagementActor._

  override def receive: Receive = {
    case CreateTaskActor(id) => {
      context.child(id.toString) match {
        case Some(actorRef) =>
          log.info(s"Actor ${id.toString} already exists")
          actorRef ! ScheduleTask(id)
        case None =>
          log.info(s"Create actor ${id.toString}")
          val taskActor = context.actorOf(Props(new TaskActor(tasksService, dataExtractorActor)), id.toString)
          taskActor ! ScheduleTask(id)
      }
    }
    case UpdateTaskActor(id) => {
      context.child(id.toString) match {
        case Some(actorRef) =>
          log.info(s"Found actor ${id.toString}")
          actorRef ! RescheduleTask(id)
        case None =>
          log.info(s"Task actor with id ${id.toString} not exists")
          self ! CreateTaskActor(id)
      }
    }
    case DeleteTaskActor(id) => {
      context.child(id.toString) match {
        case Some(actorRef) =>
          log.info(s"Killed task actor with id ${id.toString}")
          actorRef ! PoisonPill
        case None =>
          log.info(s"Task actor with id ${id.toString} not exists")
      }
    }
  }
}

