package Backend.actors

import Backend.detectors.Detector
import akka.actor.{ Actor, ActorLogging, ActorRef }
import models.DataTimeSeries

class DetectorActor(outputActor: ActorRef, detector: Detector) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ts: DataTimeSeries =>
      val result = detector.detect(ts)
      result foreach (outputActor ! _)
  }
}