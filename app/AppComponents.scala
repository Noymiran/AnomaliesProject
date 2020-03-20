package config

import akka.actor.ActorSystem

import scala.concurrent.{ ExecutionContext, Future }
import controllers._
import services._
import reactivemongo.api._
import com.typesafe.config.ConfigFactory

trait AppComponents {
  import com.softwaremill.macwire._

  lazy val config = ConfigFactory.load

  lazy val driver = new MongoDriver

  lazy val db: Future[DefaultDB] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val parsedUri = MongoConnection.parseURI(config getString "mongodb.uri")

    for {
      uri <- Future.fromTry(parsedUri)
      con = driver.connection(uri)
      dn <- Future(uri.db.get)
      db <- con.database(dn)
    } yield db
  }

  implicit lazy val system = ActorSystem("Anomaly-ActorSystem")
  lazy val tasksService: TasksService = wire[TasksMongoService]
  lazy val anomaliesTimeSeriesService: AnomaliesTimeSeriesService = wire[AnomaliesTimeSeriesService]
  lazy val mongoCRUDController = new MongoCRUDController(tasksService)(routes.TasksController.read)
  lazy val mongoAnomalyCRUDController = new MongoCRUDController(anomaliesTimeSeriesService)(routes.AnomaliesController.read)
  lazy val anomaliesController = new AnomaliesController(tasksService, anomaliesTimeSeriesService, mongoCRUDController, mongoAnomalyCRUDController)(routes.AnomaliesController.read)
  lazy val tasksController = new TasksController(tasksService, anomaliesTimeSeriesService, mongoCRUDController)(routes.TasksController.read)
}
