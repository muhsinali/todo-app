package models

import java.util.Date

import play.api.libs.json.Json

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
case class Task(dateCreated: String, dueDate: String, description: String)

case class TaskData(date: Date, description: String)



object Task {
  implicit val formatter = Json.format[Task]
}