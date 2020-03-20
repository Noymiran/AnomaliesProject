package Backend.detectors

import Backend.detectors.EgadsDetector._
import Backend.output.EgadsAnomaliesResult
import models.{ AnomalyDetails, DataTimeSeries }
import utils.Globals

import scala.collection.mutable

class EgadsSingleSeriesDetector()
    extends Detector {
  def detect(ts: DataTimeSeries): Option[EgadsAnomaliesResult] = {
    log.debug(s"(EGADS) on ${ts}")
    val props = Globals.egadsProps
    val detectedAnomalies: Option[mutable.Buffer[AnomalyDetails]] = detectAnomaly(ts, props)
    if (detectedAnomalies.nonEmpty) {
      log.debug(s"(EGADS)-${detectedAnomalies.size} anomalies for ${ts.dimsAndValueStr}")
      val result = EgadsAnomaliesResult(ts.dimensions, detectedAnomalies, ts.taskID, ts.taskName)
      Some(result)
    } else {
      None
    }
  }
}
