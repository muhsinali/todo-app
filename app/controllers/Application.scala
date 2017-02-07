package controllers

import javax.inject.Inject

import models.TaskData
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi, val messagesApi: MessagesApi) extends Controller with MongoController
    with ReactiveMongoComponents with I18nSupport {

  import scala.concurrent.ExecutionContext.Implicits.global
  val taskDAO = new TaskDAO(reactiveMongoApi)

  def index() = Action.async {implicit request => Future(Ok(views.html.main(TaskDAO.createTaskForm)))}

  def showTasks() = Action.async {implicit request => taskDAO.getAllTasks.map(tasks => Ok(Json.toJson(tasks)))}

  def createTask() = Action.async {implicit request =>
    def failure(formWithErrors: Form[TaskData]) = {
      Future(BadRequest(formWithErrors.errorsAsJson).flashing("error" -> "Could not create task"))
    }
    
    def success(taskData: TaskData) = {
      taskDAO.create(taskData)
      Future(Redirect(routes.Application.index()).flashing("success" -> "Created task"))
    }

    val form = TaskDAO.createTaskForm.bindFromRequest()
    form.fold(failure, success)
  }


}
