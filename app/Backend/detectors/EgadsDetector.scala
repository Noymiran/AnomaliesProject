package Backend.detectors

import java.util
import java.util.Properties

import com.typesafe.config.ConfigFactory
import com.yahoo.egads.control.ProcessableObjectFactory
import com.yahoo.egads.data
import com.yahoo.egads.data.Anomaly
import models.{ AnomalyDetails, DataTimeSeries, TS }
import org.joda.time.{ DateTime, DateTimeZone }
import utils._

import scala.collection.JavaConverters._
import scala.collection.mutable

object EgadsDetector extends Logging {
  lazy val config = ConfigFactory.load

  import scala.language.implicitConversions

  implicit def toDetails(anoms: Option[mutable.Buffer[Anomaly.Interval]]): Option[mutable.Buffer[AnomalyDetails]] = anoms map { anoms =>
    anoms map { v =>
      val ts = TS(
        new DateTime(v.startTime).toDateTime(DateTimeZone.UTC),
        v.actualVal.doubleValue()
      )
      AnomalyDetails(
        ts,
        Some(new DateTime(v.endTime).toDateTime(DateTimeZone.UTC)),
        Some(v.expectedVal.doubleValue()),
        v.anomalyScore.head.doubleValue()
      )
    }
  }

  def detectAnomaly(
    s: DataTimeSeries,
    properties: Properties
  ): Option[mutable.Buffer[Anomaly.Interval]] = {

    val minimunPointSeries = config getInt ("anomaly-detector.egads.anomalies.thresholds.minpointseries")
    if (s.timeSeries.size >= minimunPointSeries) {
      log.info(s"Task:${s.taskName}=>EGADS with ${s.timeSeries.size} points")

      val numAnomalies = config getInt ("anomaly-detector.egads.anomalies.thresholds.num")
      val minAnomaliesScore = config getInt ("anomaly-detector.egads.anomalies.thresholds.score")

      val po = ProcessableObjectFactory.create(makeEgadsTimeSeries(s), properties)
      po.process()
      val results = po.result().asInstanceOf[util.ArrayList[Anomaly]].asScala

      log.debug(s"Egads anomaly result size ${results.length}")

      if (results.isEmpty) None
      else {
        val anomalies = results flatMap (_.intervals.asScala)
        log.debug(s"Non expected values found for task ${s.taskName}")

        if (anomalies.length >= numAnomalies) {
          log.debug(s"Suspect value detected for task ${s.taskName} ")
          val filteredAnoms = anomalies.filter { v =>
            v.anomalyScore.nonEmpty && v.anomalyScore.head >= minAnomaliesScore
          }

          Some(filteredAnoms)
        } else None
      }
    } else {
      log.info(s"Task:${s.taskID}=>Series for ${s.dimsAndValueStr}  is too small (${s.timeSeries.size} < Min($minimunPointSeries)")
      None
    }
  }

  def makeEgadsTimeSeries(dataTimeSeries: DataTimeSeries): data.TimeSeries = {
    val timeSeries: List[(Long, BigDecimal)] = dataTimeSeries.timeSeries.map(pair =>
      (pair.date.getMillis, pair.value)).sortBy(_._1)

    val timestamps = timeSeries.unzip._1 map (time => time)
    val values = timeSeries.unzip._2 map (value => value.floatValue())
    new com.yahoo.egads.data.TimeSeries(timestamps.toArray, values.toArray)
  }
}
