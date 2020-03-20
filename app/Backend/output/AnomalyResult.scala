package Backend.output

import java.util.UUID

import models.{ AnomalyDetails, Dimension, Dimensions }
import utils.Globals._

import scala.collection.mutable

sealed trait Formatter {
  def formatMessageOutput(implicit d: AnomalyDetails): String
}

abstract class AnomaliesResult(
  val dimensions: List[Dimension],
  val anomalies: Option[mutable.Buffer[AnomalyDetails]],
  val taskID: UUID,
  val taskName: String
) extends Formatter
    with Dimensions {

  protected def format(): Option[mutable.Buffer[String]] =
    anomalies map { anomaly => anomaly map { implicit d => formatMessageOutput } }

  override def toString: String =
    (format map (v => v.mkString("\n"))).getOrElse("")

}

case class AlertAnomaliesResult(
  override val dimensions: List[Dimension],
  override val anomalies: Option[mutable.Buffer[AnomalyDetails]],
  override val taskID: UUID,
  override val taskName: String

)
    extends AnomaliesResult(dimensions, anomalies, taskID, taskName) {

  override def formatMessageOutput(implicit d: AnomalyDetails): String = {
    val dateTime = dayTimeFormatter.print(d.ts.date)
    s"(ALERT:${taskName}) with ID ${taskID} at $dateTime "
  }
}

case class EgadsAnomaliesResult(
  override val dimensions: List[Dimension],
  override val anomalies: Option[mutable.Buffer[AnomalyDetails]],
  override val taskID: UUID,
  override val taskName: String
)
    extends AnomaliesResult(dimensions, anomalies, taskID, taskName) {

  override def formatMessageOutput(implicit d: AnomalyDetails): String = {
    val confidence = d.confidence.toString
    val dateTime = dayTimeFormatter.print(d.ts.date)

    s"(EGADS: task name:${taskName}) with ID ${taskID} at $dateTime on $dimsAndValueStr expected:${d.previousValue.getOrElse("NA")} " +
      s"confidence:$confidence"
  }
}
