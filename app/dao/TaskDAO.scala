package dao

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

import models.{Task, TaskData}
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}



class TaskDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends Controller
    with MongoController with ReactiveMongoComponents {
  private val sdf = new SimpleDateFormat("dd-MM-yyyy")
  private def tasksCollection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("tasks"))


  def all: Future[List[Task]] = {
    tasksCollection.flatMap(_.find(Json.obj()).cursor[Task](ReadPreference.primaryPreferred)
        .collect[List](Int.MaxValue, Cursor.FailOnError[List[Task]]())
    )
  }

  def create(t: TaskData): Future[WriteResult] = {
    tasksCollection.flatMap(_.insert(Task(TaskDAO.generateID, t.title, t.description, sdf.format(new Date()), sdf.format(t.dueDate))))
  }

  // Used to populate database with tasks at startup
  def create(title: String, description: String, dueDate: Date): Future[WriteResult] = {
    tasksCollection.flatMap(_.insert(Task(TaskDAO.generateID, title, description, sdf.format(new Date()), sdf.format(dueDate))))
  }

  def drop(): Future[Boolean] = tasksCollection.flatMap(_.drop(failIfNotFound = true))

  def findById(id: Int): Future[Option[Task]] = {
    tasksCollection.flatMap(_.find(Json.obj("id" -> id)).one[Task](ReadPreference.primaryPreferred))
  }

  def remove(id: Int): Future[WriteResult] = tasksCollection.flatMap(_.remove(Json.obj("id" -> id)))

  def update(taskData: TaskData): Future[UpdateWriteResult] = {
    val id = taskData.id.get
    for {
      tasks <- tasksCollection
      taskFound <- findById(id)
      writeResult <- tasks.update(Json.obj("id" -> id),
        Task(id, taskData.title, taskData.description, taskFound.get.dateCreated, sdf.format(taskData.dueDate)))
    } yield writeResult
  }

}


object TaskDAO {
  val createTaskForm = {
    import play.api.data.Forms._
    Form(
      mapping(
        "id" -> optional(number),
        "title" -> nonEmptyText,
        "description" -> nonEmptyText,
        "dueDate" -> date
      )(TaskData.apply)(TaskData.unapply)
    )
  }

  private var numTasksCreated = 0
  def generateID: Int = {
    numTasksCreated += 1
    numTasksCreated
  }

}