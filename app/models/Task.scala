package models

import java.util.Date

import play.api.libs.json.Json

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
case class Task(title: String, description: String, dateCreated: String, dueDate: String)

case class TaskData(title: String, description: String, dueDate: Date)



object Task {
  implicit val formatter = Json.format[Task]
}

object TaskData {
  // Used in ApplicationInterceptor
  implicit val formatter = Json.format[TaskData]
}