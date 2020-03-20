package requests

import models.{ DataTimeSeries, Dimension, TS, Task }
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json._

trait SeriesConverter[T] {
  def task: Task

  def buildQuery(): String

  def buildTimeSeries(response: T): Option[List[DataTimeSeries]]
}

sealed abstract class MetricName(val name: String)

object MetricName {

  case object UIGraph extends MetricName("Front End error")

  case object AvgOverhead extends MetricName("Avg overhead")

  case object AvgHealthServiceFailed extends MetricName("Avg health service failed")

  case object ELBUnhealthyHostAVG extends MetricName("AWS ELB un healthy host count avg")

  case object ELBHealthyHostAVG extends MetricName("AWS ELB healthy host count avg")

  case object AvgHealthServicePassing extends MetricName("Avg health service passing")

  case object AvgLatency extends MetricName("Avg latency")

  case object QPS extends MetricName("QPS")

  case object AdNetLatency extends MetricName("Adnet latency 95 percentile")

  private val states = List[MetricName](UIGraph, AvgOverhead, AvgHealthServiceFailed, ELBUnhealthyHostAVG, ELBHealthyHostAVG, AvgHealthServicePassing, AvgLatency, QPS, AdNetLatency).map(a => a.name -> a).toMap

  def apply(name: String): Option[MetricName] = states.get(name)
}

case class PResultVector(metric: Map[String, String], value: (BigDecimal, BigDecimal))

object PResultVector {

  implicit def tuple2Format[A, B](implicit a: Format[A], b: Format[B]): Format[Tuple2[A, B]] = new Format[Tuple2[A, B]] {
    def writes(tuple: Tuple2[A, B]) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))

    def reads(json: JsValue): JsResult[Tuple2[A, B]] = json match {
      case JsArray(arr) if arr.size == 2 => for {
        av <- a.reads(arr(0))
        bv <- b.reads(arr(1))
      } yield (av, bv)
      case _ => JsError("Expected array of three elements")
    }
  }

  implicit val formatTuple = tuple2Format[BigDecimal, BigDecimal]
  implicit val PResultVectorFormat: Format[PResultVector] = Json.format[PResultVector]

}

case class PDataVector(resultType: String, result: List[PResultVector])

object PDataVector {

  implicit val PDataVectorFormat: Format[PDataVector] = Json.format[PDataVector]

}

case class PrometheusResultVector(status: String, data: PDataVector)

object PrometheusResultVector {

  implicit val PrometheusResultVectorFormat: Format[PrometheusResultVector] = Json.format[PrometheusResultVector]

}

case class PResultMatrix(metric: Map[String, String], values: List[(BigDecimal, BigDecimal)])

object PResultMatrix {
  implicit val PResultMatrixFormat: Format[PResultMatrix] = Json.format[PResultMatrix]

}

case class PDataMatrix(resultType: String, result: List[PResultMatrix])

object PDataMatrix {
  implicit val PDataMatrixFormat: Format[PDataMatrix] = Json.format[PDataMatrix]
}

case class PrometheusResultMatrix(status: String, data: PDataMatrix)

object PrometheusResultMatrix {
  implicit val PrometheusResultMatrixFormat: Format[PrometheusResultMatrix] = Json.format[PrometheusResultMatrix]
}

final class PrometheusSeriesConverter(val task: Task) extends SeriesConverter[String] {

  private def addTimeToQuery(): String = {
    val unit = task.intervalPeriodBackUnit
    val length = task.intervalPeriodBackLength
    "[" + length + unit + "]"
  }

  private def addTimeAndRate(): String = {
    "rate(" + task.metric.value + addTimeToQuery + ")"
  }

  private def addAvgTimeAndRate(): String = {
    "avg(" + addTimeAndRate + ")"
  }

  override def buildQuery(): String = MetricName(task.metric.name).getOrElse("NonValid") match {
    case MetricName.AvgOverhead | MetricName.ELBUnhealthyHostAVG | MetricName.ELBHealthyHostAVG | MetricName.AvgLatency | MetricName.QPS => task.metric.value + addTimeToQuery
    case MetricName.AvgHealthServiceFailed => addTimeAndRate
    case MetricName.AvgHealthServicePassing => addAvgTimeAndRate
    case MetricName.AdNetLatency => "sort_desc(" + addAvgTimeAndRate + "by (network))"
    case MetricName.UIGraph => "sum(" + addTimeAndRate() + ")"
  }

  def convToDimension(values: Map[String, String]): Iterable[Dimension] = values.map(x => Dimension(x._1, x._2))

  def convListToTS(values: List[(BigDecimal, BigDecimal)]): List[TS] = values.map(x => TS(DateTime.now().withMillis((x._1 * 1000L).toLong).withMillisOfSecond(0).toDateTime(DateTimeZone.UTC), x._2))

  override def buildTimeSeries(response: String): Option[List[DataTimeSeries]] = {
    val json = Json.parse(response)
    val resultType = (json \ "data" \ "resultType").as[String]
    resultType match {
      case "vector" =>
        val vector = json.as[PrometheusResultVector]
        val dimensions = vector.data.result.map {
          v => convToDimension(v.metric)
        }
        val times = vector.data.result.map({
          v => convListToTS(List(v.value))
        })
        task.uuid.map {
          id =>
            dimensions.map(d =>
              DataTimeSeries(id, task.name, d.toList, times.flatten))
        }
      case "matrix" =>
        val matrix = json.as[PrometheusResultMatrix]
        val dimensions = matrix.data.result.map {
          v => convToDimension(v.metric)
        }
        val times = matrix.data.result.map({
          v => convListToTS(v.values)
        })
        task.uuid.map {
          id =>
            dimensions.map(d =>
              DataTimeSeries(id, task.name, d.toList, times.flatten))
        }
    }
  }
}
