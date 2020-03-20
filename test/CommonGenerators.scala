package utils

import org.scalacheck._
import models._
import org.scalacheck.Arbitrary.arbitrary

trait CommonGenerators {
  val NameGenerator = Gen.alphaStr.map(n => n + "z")
  val BigDecimalGenerator = for {
    n <- arbitrary[Double]
  } yield (BigDecimal(n, java.math.MathContext.DECIMAL128))

  val outputChannelGenerator = for { name <- NameGenerator; value <- NameGenerator } yield OutputChannel(name, value)
  val metricGenerator = for { name <- NameGenerator; value <- NameGenerator } yield Metric(name, value)

  val TaskGenerator = for (
    name <- NameGenerator;
    detectorName <- NameGenerator;
    dataResource <- NameGenerator;
    intervalSchedulerUnit <- NameGenerator;
    intervalSchedulerLength <- NameGenerator;
    outputChannel <- outputChannelGenerator;
    intervalPeriodBackUnit <- NameGenerator;
    intervalPeriodBackLength <- NameGenerator;
    metric <- metricGenerator
  ) yield Task(
    None,
    name,
    detectorName,
    dataResource,
    intervalSchedulerUnit,
    intervalSchedulerLength,
    outputChannel,
    intervalPeriodBackUnit,
    intervalPeriodBackLength,
    metric
  )
}