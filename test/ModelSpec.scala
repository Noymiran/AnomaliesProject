package models

import java.util.UUID

import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest.prop.PropertyChecks
import play.api.libs.json._
import utils.CommonGenerators

class ModelSpec extends WordSpecLike with Matchers with PropertyChecks with CommonGenerators {

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSize = 1, maxSize = 100, minSuccessful = 100, workers = 5)

  "A Model" must {
    "provide class Task" which {
      "should be serializable to/from json object when id is undefined" in {
        forAll(TaskGenerator) {
          task: Task =>
            val json = Json.obj(
              "name" -> JsString(task.name),
              "detectorName" -> JsString(task.detectorName),
              "dataResource" -> JsString(task.dataResource),
              "intervalSchedulerUnit" -> JsString(task.intervalSchedulerUnit),
              "intervalSchedulerLength" -> JsString(task.intervalSchedulerLength),
              "outputChannel" -> task.outputChannel,
              "intervalPeriodBackUnit" -> JsString(task.intervalPeriodBackUnit),
              "intervalPeriodBackLength" -> JsString(task.intervalPeriodBackLength),
              "metric" -> task.metric
            )

            Json.toJson(task) should be(json)

            Json.parse(Json.prettyPrint(json)).as[Task] should be(task)
        }
      }

      "should be serializable to/from json object when id is defined" in {
        forAll(TaskGenerator) {
          task: Task =>
            val id = UUID.randomUUID()
            val newTask = Task(Some(id), task.name, task.detectorName, task.dataResource, task.intervalSchedulerUnit, task.intervalSchedulerLength, task.outputChannel, task.intervalPeriodBackUnit, task.intervalPeriodBackLength, task.metric)
            val json = Json.obj(
              "uuid" -> id,
              "name" -> JsString(task.name),
              "detectorName" -> JsString(task.detectorName),
              "dataResource" -> JsString(task.dataResource),
              "intervalSchedulerUnit" -> JsString(task.intervalSchedulerUnit),
              "intervalSchedulerLength" -> JsString(task.intervalSchedulerLength),
              "outputChannel" -> task.outputChannel,
              "intervalPeriodBackUnit" -> JsString(task.intervalPeriodBackUnit),
              "intervalPeriodBackLength" -> JsString(task.intervalPeriodBackLength),
              "metric" -> task.metric
            )

            Json.toJson(newTask) should be(json)

            Json.parse(Json.prettyPrint(json)).as[Task] should be(newTask)
        }

      }
    }
  }
}