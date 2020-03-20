package config

import services.TasksService

import services._

trait TestAppComponents extends AppComponents {

  import com.softwaremill.macwire._

  override lazy val tasksService: TasksService = wire[TestTasksService]

}