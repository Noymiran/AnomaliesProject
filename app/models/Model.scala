package models

import java.util.UUID

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json._

import scala.collection.mutable

case class Dimension(name: String, value: String)

object Dimension {
  implicit val DimensionFormat = Json.format[Dimension]
}

object DateTimeFormatter {
  private lazy val ISODateTimeFormatter = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  private lazy val ISODateTimeParser = ISODateTimeFormat.dateTimeParser

  implicit val dateTimeFormatter = new Format[DateTime] {
    def reads(j: JsValue): JsSuccess[DateTime] = JsSuccess(ISODateTimeParser.parseDateTime(j.as[String]))

    def writes(o: DateTime): JsValue = JsString(ISODateTimeFormatter.print(o))
  }
}

case class TS(date: DateTime, value: BigDecimal)

object TS {
  import DateTimeFormatter.dateTimeFormatter
  implicit val TSFormat = Json.format[TS]
}

case class OutputChannel(name: String, value: String)

object OutputChannel {
  implicit val OutputChannelFormat = Json.format[OutputChannel]

}

case class Metric(name: String, value: String)

object Metric {
  implicit val MetricFormat = Json.format[Metric]

}

trait Dimensions {
  val dimensions: List[Dimension]

  val dims: String = dimensions.map(_.name).mkString("&")
  val dimsValues: String = dimensions.map(_.value).mkString("&")
  val dimsAndValueStr: String = (dimensions.map(v => s"${v.name}:${v.value}").mkString("&"))
  val dimensionsToMap: Map[String, String] = dimensions.map(v => (v.name, v.value)).toMap
}

case class Task(
  uuid: Option[UUID],
  name: String,
  detectorName: String,
  dataResource: String,
  intervalSchedulerUnit: String,
  intervalSchedulerLength: String,
  outputChannel: OutputChannel,
  intervalPeriodBackUnit: String,
  intervalPeriodBackLength: String,
  metric: Metric
)

object Task {
  implicit val TaskFormat = Json.format[Task]

  implicit object TaskIdentity extends Identity[Task, UUID] {
    val name = "uuid"

    def of(entity: Task): Option[UUID] = entity.uuid

    def set(entity: Task, id: UUID): Task = entity.copy(uuid = Option(id))

    def clear(entity: Task): Task = entity.copy(uuid = None)

    def next: UUID = UUID.randomUUID()
  }

}
object DataTimeSeries {
  implicit val dataTimeSeriesFormat = Json.format[DataTimeSeries]
}

case class DataTimeSeries(
    taskID: UUID,
    taskName: String,
    dimensions: List[Dimension],
    timeSeries: List[TS]
) extends Dimensions {

  val length: Int = timeSeries.size
  val first: Option[TS] = timeSeries.headOption
  val last: Option[TS] = timeSeries.lastOption

  val firstValue: Option[Double] = first.map(_.value.toDouble)
  val lastValue: Option[Double] = last.map(_.value.toDouble)

  val avg: Double = if (length > 0) {
    val total = timeSeries.foldLeft(0.0)((v1, v2) => v1 + v2.value.doubleValue())
    total / timeSeries.size.doubleValue()
  } else 0
}

case class AnomalyDetails(
  ts: TS,
  previousTimestamp: Option[DateTime] = None,
  previousValue: Option[BigDecimal] = None,
  confidence: BigDecimal
)

object AnomalyDetails {
  import DateTimeFormatter.dateTimeFormatter
  implicit val AnomalyDetailsFormat = Json.format[AnomalyDetails]
}

case class AnomaliesTimeSeries(
  uuid: Option[UUID],
  taskID: UUID,
  timeSeries: Option[mutable.Buffer[AnomalyDetails]],
  dimensions: List[Dimension]
) extends Dimensions

object AnomaliesTimeSeries {
  implicit val AnomaliesTimeSeriesFormat = Json.format[AnomaliesTimeSeries]

  implicit object AnomaliesTimeSeriesIdentity extends Identity[AnomaliesTimeSeries, UUID] {
    val name = "uuid"

    def of(entity: AnomaliesTimeSeries): Option[UUID] = entity.uuid

    def set(entity: AnomaliesTimeSeries, id: UUID): AnomaliesTimeSeries = entity.copy(uuid = Option(id))

    def clear(entity: AnomaliesTimeSeries): AnomaliesTimeSeries = entity.copy(uuid = None)

    def next: UUID = UUID.randomUUID()
  }

}