package controllers

import javax.inject.Inject

import models.TaskData
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi, val messagesApi: MessagesApi)
                           (implicit ec: ExecutionContext) extends Controller with MongoController
    with ReactiveMongoComponents with I18nSupport {

  val taskDAO = new TaskDAO(reactiveMongoApi)

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
    TaskDAO.createTaskForm.bindFromRequest().fold(failure, success)
  }

  // TODO get the flash scope to show up
  def deleteTask(id: Int) = Action.async {implicit request =>
    taskDAO.remove(id).map { writeResult =>
      val flashMessage = if (writeResult.ok) "success" -> s"Deleted task with id $id" else "error" -> s"Could not delete task with id $id"
      Redirect(routes.Application.index()).flashing(flashMessage)
    }
  }


}
