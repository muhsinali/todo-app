package models

import java.util.Date

import play.api.libs.json.Json

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
case class Task(dueDate: Date, description: String)

case class TaskData(id: Option[Int], date: Date, description: String)



object Task {
  implicit val formatter = Json.format[Task]
}