package Backend.actors

import java.util.UUID

import Backend.actors.DataExtractorActor.ExtractData
import akka.actor.{ Actor, ActorLogging, ActorRef, Timers }
import models.Task
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.TasksService

import scala.concurrent.duration._

object TaskActor {

  final case class ScheduleTask(id: UUID)

  final case class RescheduleTask(id: UUID)

  final case class SendToExtractor(task: Task)

}

class TaskActor(tasksService: TasksService, dataExtractorActor: ActorRef) extends Actor with Timers with ActorLogging {

  import TaskActor._

  override def receive: Receive = {
    case ScheduleTask(id) => {
      log.info("Schedule task")
      tasksService.read(id).map {
        _.map {
          task =>
            {
              val finite = Duration(task.intervalSchedulerLength + " " + task.intervalSchedulerUnit)
              Some(finite).collect {
                case d: FiniteDuration =>
                  timers.startPeriodicTimer(id.toString, SendToExtractor(task), d)
              }
            }
        }
      }
    }
    case RescheduleTask(id) => {
      timers.cancel(id.toString)
      self ! ScheduleTask(id)
    }

    case SendToExtractor(task) => {
      log.info("Send data to extractor")
      dataExtractorActor ! ExtractData(task)
    }
  }
}
