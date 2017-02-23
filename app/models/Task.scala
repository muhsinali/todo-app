package models

import java.util.Date

import play.api.libs.json.Json



case class Task(id: Int, title: String, description: String, dateCreated: String, dueDate: String)

case class TaskData(id: Option[Int], title: String, description: String, dueDate: Date)



object Task {
  implicit val formatter = Json.format[Task]
}

object TaskData {
  // Used in ApplicationInterceptor.onStartup() to populate database with Tasks on startup
  implicit val formatter = Json.format[TaskData]
}