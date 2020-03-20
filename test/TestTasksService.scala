package services

import models.Task
import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

class TestTasksService extends TestGenericCRUD[Task, UUID] with TasksService
