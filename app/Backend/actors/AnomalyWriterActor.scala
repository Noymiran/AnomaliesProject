package Backend.actors

import Backend.output.{ AnomaliesResult, Email }
import akka.actor.{ Actor, ActorLogging }
import com.typesafe.config.Config
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.TasksService

class AnomalyWriterActor(tasksService: TasksService) extends Actor with ActorLogging {
  implicit val config: Config = context.system.settings.config

  override def receive: Receive = {
    case a: AnomaliesResult =>
      tasksService.read(a.taskID).map {
        _.map {
          task =>
            {
              task.outputChannel.name match {
                case "Email" =>
                  Email(task.outputChannel.value.split(",").toVector).writer.write(a)
              }
            }
        }
      }
  }
}
