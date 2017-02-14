package controllers

import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject

import models.TaskData
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future
import scala.io.Source

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi, val messagesApi: MessagesApi) extends Controller with MongoController
    with ReactiveMongoComponents with I18nSupport {

  import scala.concurrent.ExecutionContext.Implicits.global
  val taskDAO = new TaskDAO(reactiveMongoApi)

  onStartup()


  def onStartup(): Unit = {
    def getJsonProperty(jsValue: JsValue, field: String) = (jsValue \ field).as[String]

    val file = new File("public/exampleTask.json")
    assert(file.exists() && file.isFile && file.canRead)

    val source = Source.fromFile(file)
    val parsedJson = Json.parse(source.mkString)
    val title = getJsonProperty(parsedJson, "title")
    val description = getJsonProperty(parsedJson, "description")
    val dueDate = getJsonProperty(parsedJson, "dueDate")
    source.close()

    taskDAO.create(title, description, new SimpleDateFormat("yyyy-MM-dd").parse(dueDate)).onFailure{
      case throwable => Logger.error("Failed to create task object", throwable)
    }
  }


  def index() = Action.async {implicit request =>
    taskDAO.getAllTasks.map(tasks => Ok(views.html.main(tasks, TaskDAO.createTaskForm)))
  }

  def fileNotFound() = Action {implicit request => NotFound(views.html.notFoundPage())}

  def createTask() = Action.async {implicit request =>
    def failure(formWithErrors: Form[TaskData]) = {
      taskDAO.getAllTasks.map(tasks => BadRequest(views.html.main(tasks, formWithErrors)))
    }
    
    def success(taskData: TaskData) = {
      taskDAO.create(taskData)
      Future(Redirect(routes.Application.index()).flashing("success" -> "Created task"))
    }

    val form = TaskDAO.createTaskForm.bindFromRequest()
    form.fold(failure, success)
  }


}
