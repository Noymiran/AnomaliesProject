package services

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import play.api.libs.json._
import play.api.libs.json.Json._

import org.junit.runner.RunWith
import org.scalatest.{ WordSpecLike, Matchers }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.junit.JUnitRunner
import org.scalacheck._
import utils._

class TestGenericCRUDSpec extends WordSpecLike with Matchers with PropertyChecks with CommonGenerators with ScalaFutures {

  import models._

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSize = 1, maxSize = 100, minSuccessful = 100, workers = 5)

  "A TestCRUDService" must {

    "not find existing instance for new entity" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator) { (task: Task) =>
        service.search(Json.toJson(task).as[JsObject], 1).futureValue shouldBe empty
      }
    }

    "create new entitity" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator) { (task: Task) =>
        val id: UUID = service.create(task).futureValue.right.get
        id should not be null
      }
    }

    "delete new entitity" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator) { (task: Task) =>
        val id: UUID = service.create(task).futureValue.right.get
        id should not be null
        val id2: UUID = service.delete(id).futureValue.right.get
        id should be(id2)
      }
    }

    "update new entitity" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator, NameGenerator) { (task: Task, name: String) =>

        val id: UUID = service.create(task).futureValue.right.get
        id should not be null

        val newTask = task.copy(task.uuid, name)
        val id2: UUID = service.update(id, newTask).futureValue.right.get
        id should be(id2)
      }
    }

    "create entity only once for the same id" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator) { (task: Task) =>
        val id: UUID = service.create(task).futureValue.right.get
        service.create(task).futureValue shouldBe Right(id)
      }
    }

    "find existing entity by id" in {
      val service = new TestGenericCRUD[Task, UUID]
      forAll(TaskGenerator) { (task: Task) =>
        val id: UUID = service.create(task).futureValue.right.get
        service.read(id).futureValue.get.copy(uuid = None) shouldBe task
      }
    }
  }
}
