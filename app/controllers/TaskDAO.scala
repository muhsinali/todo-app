package controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

import models.{Task, TaskData}
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class TaskDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends Controller
    with MongoController with ReactiveMongoComponents {
  val sdf = new SimpleDateFormat("dd-MM-yyyy")

  def create(t: TaskData): Future[WriteResult] = {
    tasksFuture.flatMap(_.insert(Task(t.title, t.description, sdf.format(new Date()), sdf.format(t.dueDate))))
  }

  // Used to populate database with tasks at startup
  def create(title: String, description: String, dueDate: Date): Future[WriteResult] = {
    tasksFuture.flatMap(_.insert(Task(title, description, sdf.format(new Date()), sdf.format(dueDate))))
  }

  def drop(): Future[Boolean] = tasksFuture.flatMap(_.drop(failIfNotFound = true))

  def getAllTasks: Future[List[Task]] = {
    tasksFuture.flatMap(_.find(Json.obj()).cursor[Task](ReadPreference.primaryPreferred)
        .collect[List](Int.MaxValue, Cursor.FailOnError[List[Task]]())
    )
  }

  def tasksFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("tasks"))
}


object TaskDAO {
  val createTaskForm = {
    import play.api.data.Forms._
    Form(
      mapping(
        "title" -> nonEmptyText,
        "description" -> nonEmptyText,
        "dueDate" -> date
      )(TaskData.apply)(TaskData.unapply)
    )
  }

}