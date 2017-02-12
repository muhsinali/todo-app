package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import javax.inject.Inject

import models.{Task, TaskData}
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class TaskDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends Controller
    with MongoController with ReactiveMongoComponents {

  def create(t: TaskData): Future[WriteResult] = {
    val sdf = new SimpleDateFormat("dd-MM-yyyy")
    tasksFuture.flatMap(_.insert(Task(sdf.format(new Date()), sdf.format(t.date), t.description)))
  }

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
        "dueDate" -> date,
        "description" -> nonEmptyText
      )(TaskData.apply)(TaskData.unapply)
    )
  }

}