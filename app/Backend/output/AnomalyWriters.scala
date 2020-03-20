package Backend.output

import scala.util.{ Success, Try }

trait AnomalyWriter {
  def write(anomalyResult: AnomaliesResult): Try[Unit]
}

class EmailWriter(emailAddresses: Vector[String]) extends AnomalyWriter {
  override def write(anomalyResult: AnomaliesResult): Try[Unit] = {
    anomalyResult.anomalies.map(ts => {
      val found = ts.length
      val subject = s"$found anomalies found for task: taskID: ${anomalyResult.taskID}"

      EmailClient.sendMail(subject, anomalyResult.toString, emailAddresses)
    })
    Success(())
  }
}

trait OutputChannelsWriter {
  val writer: AnomalyWriter
}

case class Email(emailAddresses: Vector[String]) extends OutputChannelsWriter {
  override val writer: AnomalyWriter = new EmailWriter(emailAddresses)
}