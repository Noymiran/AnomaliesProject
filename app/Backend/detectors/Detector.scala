package Backend.detectors

import Backend.output.AnomaliesResult
import models.DataTimeSeries

trait Detector {

  def detect(timeSeries: DataTimeSeries): Option[AnomaliesResult]
}

