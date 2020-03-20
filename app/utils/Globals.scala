package utils

import java.util.Properties

import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter, ISODateTimeFormat }

object Globals {

  val isoFormat: DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()
  val segmentDateFormat: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSSZ").withZoneUTC()
  val segmentDateFormat2: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ").withZoneUTC()
  val dayFormatter: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd")
  val dayTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd.HH")

  val egadsProps: Properties = {
    val is = this.getClass.getResourceAsStream("/egads-config.ini")
    val props = new Properties()
    props.load(is)
    props
  }
}
