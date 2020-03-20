package controllers

import java.util.UUID

import config.WithTestApplication
import models._
import org.scalacheck._
import org.scalatest.{ Matchers, WordSpecLike }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.test._

class ApplicationSpec extends WordSpecLike with Matchers with PropertyChecks with utils.CommonGenerators with ScalaFutures {

  import Helpers._

  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSize = 1, maxSize = 100, minSuccessful = 100, workers = 1)

  val UUIDRegex = """/tasks/(\w{8}-\w{4}-\w{4}-\w{4}-\w{12})""".r

  "Application" should {

    "send 404 on a bad request" in new WithTestApplication {
      val result = route(FakeRequest(GET, "/boum")).get
      status(result) should be(404)
    }

    "provide an API to create task - mocked db" in new WithTestApplication {
      forAll(TaskGenerator) {
        task: Task =>
          val createRequest = FakeRequest(POST, "/tasks").withJsonBody(Json.toJson(task))
          val createResponse = route(createRequest).get
          status(createResponse) should be(CREATED)
          contentType(createResponse) shouldBe None
          contentAsString(createResponse) should be("")
          val locationHeader = header(LOCATION, createResponse).get
          locationHeader should fullyMatch regex UUIDRegex
          val UUIDRegex(uuid) = locationHeader
      }
    }

    "provide an API to read task - mocked db" in new WithTestApplication {
      forAll(TaskGenerator) {
        task: Task =>
          createTask(task) map { uuid =>
            val findByIdRequest = FakeRequest(GET, s"/tasks/$uuid")
            val findByIdResponse = route(findByIdRequest).get
            status(findByIdResponse) should be(OK)
            contentType(findByIdResponse) shouldBe Some("application/json")
            val storedTask = Json.parse(contentAsString(findByIdResponse)).as[Task]
          }
      }
    }

    "provide an API to update task - mocked db" in new WithTestApplication {
      forAll(TaskGenerator) {
        task: Task =>
          createTask(task) map { uuid =>
            val findByIdRequest = FakeRequest(GET, s"/tasks/$uuid")
            val findByIdResponse = route(findByIdRequest).get
            status(findByIdResponse) should be(OK)
            contentType(findByIdResponse) shouldBe Some("application/json")
            val storedTask = Json.parse(contentAsString(findByIdResponse)).as[Task]
            storedTask should be(task.copy(uuid = Some(uuid)))
          }
      }
    }

    "provide an API to delete task - mocked db" in new WithTestApplication {
      forAll(TaskGenerator) {
        task: Task =>
          createTask(task) map { uuid =>
            val deleteRequest = FakeRequest(DELETE, s"/tasks/$uuid")
            val deleteResponse = route(deleteRequest).get
            status(deleteResponse) should be(OK)
            header(LOCATION, deleteResponse) shouldBe None
            // Test again if DELETE is idempotent
            val deleteResponse2 = route(deleteRequest).get
            status(deleteResponse2) should be(OK)
            header(LOCATION, deleteResponse2) shouldBe None
            // Test again FIND BY ID
            val findByIdResponse = route(FakeRequest(GET, s"/tasks/$uuid")).get
            status(findByIdResponse) should be(NOT_FOUND)
          }
      }
    }

    "provide an API to search tasks - mocked db" in new WithTestApplication {
      forAll(TaskGenerator) {
        task: Task =>
          createTask(task) map { uuid =>
            val query = Json.toJson(services.SearchQuery(
              name = Some(task.name),
              detectorName = Some(task.detectorName),
              dataResource = Some(task.dataResource),
              intervalSchedulerUnit = Some(task.intervalSchedulerUnit),
              intervalSchedulerLength = Some(task.intervalSchedulerLength),
              intervalPeriodBackUnit = Some(task.intervalPeriodBackUnit),
              intervalPeriodBackLength = Some(task.intervalPeriodBackLength),
              metric = Some(task.metric.name)
            ))
            val findByCiteriaRequest = FakeRequest(POST, s"/tasks/search").withJsonBody(query)
            val findByCiteriaResponse = route(findByCiteriaRequest).get
            status(findByCiteriaResponse) should be(OK)
            val foundTasks = Json.parse(contentAsString(findByCiteriaResponse)).as[List[Task]]
            foundTasks.size should be >= (1)
            foundTasks should contain(task.copy(uuid = Some(uuid)))
          }
      }
    }

  }

  def createTask(task: Task): Option[UUID] = {
    route(FakeRequest(POST, "/tasks").withJsonBody(Json.toJson(task)))
      .flatMap(header(LOCATION, _))
      .map(s => {
        val UUIDRegex(uuid) = s; UUID.fromString(uuid)
      })
  }
}
