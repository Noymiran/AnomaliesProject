package services

import java.util.UUID

import models.{ AnomaliesTimeSeries, Task }
import play.api.libs.json._
import utils.Criteria

import scala.concurrent.{ ExecutionContext, Future }

case class SearchQuery(
  name: Option[String],
  detectorName: Option[String],
  dataResource: Option[String],
  intervalSchedulerUnit: Option[String],
  intervalSchedulerLength: Option[String],
  intervalPeriodBackUnit: Option[String],
  intervalPeriodBackLength: Option[String],
  metric: Option[String]
)

object SearchQuery {
  implicit val format = Json.format[SearchQuery]
}

trait TasksService extends GenericCRUD[Task, UUID] {

  def search(query: SearchQuery, limit: Int)(implicit ec: ExecutionContext): Future[Traversable[Task]] = {

    import utils.Criteria._
    val name = query.name.map(name => If("name" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val detectorName = query.detectorName.map(name => If("detectorName" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val dataResource = query.dataResource.map(name => If("dataResource" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val intervalSchedulerUnit = query.intervalSchedulerUnit.map(name => If("intervalSchedulerUnit" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val intervalSchedulerLength = query.intervalSchedulerLength.map(name => If("intervalSchedulerLength" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val intervalPeriodBackUnit = query.intervalPeriodBackUnit.map(name => If("intervalPeriodBackUnit" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val intervalPeriodBackLength = query.intervalPeriodBackLength.map(name => If("intervalPeriodBackLength" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val metric = query.metric.map(name => If("metric.name" -> Regex("^.*?" + name + ".*$", Some("i"))))
    val criteria: Option[Criteria] = name && detectorName && dataResource && intervalSchedulerUnit && intervalSchedulerLength && intervalPeriodBackUnit && intervalPeriodBackLength && metric

    criteria match {
      case Some(c) =>
        search(c, limit)
      case None =>
        search(Json.obj(), limit)
    }
  }

}

import reactivemongo.api.DB
import reactivemongo.play.json.collection._

class TasksMongoService(db: Future[DB])
    extends MongoGenericCRUD[Task, UUID] with TasksService {
  override def collection(implicit ec: ExecutionContext): Future[JSONCollection] = db.map(_.collection("tasks"))
}

class AnomaliesTimeSeriesService(db: Future[DB])
    extends MongoGenericCRUD[AnomaliesTimeSeries, UUID] {
  override def collection(implicit ec: ExecutionContext): Future[JSONCollection] = db.map(_.collection("anomalies_timeSeries"))

}