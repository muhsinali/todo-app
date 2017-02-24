package controllers

import java.text.SimpleDateFormat
import javax.inject.Inject

import dao.TaskDAO
import models.TaskData
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}

import scala.concurrent.{ExecutionContext, Future}


class Application @Inject()(val reactiveMongoApi: ReactiveMongoApi, val messagesApi: MessagesApi)
                           (implicit ec: ExecutionContext) extends Controller with MongoController
    with ReactiveMongoComponents with I18nSupport {

  val taskDAO = new TaskDAO(reactiveMongoApi)

  // TODO get the flash scope to show up
  def deleteTask(id: Int) = Action.async {implicit request =>
    taskDAO.remove(id).map { writeResult =>
      val flashMessage = if (writeResult.ok) "success" -> s"Deleted task with id $id" else "error" -> s"Could not delete task with id $id"
      Redirect(routes.Application.index()).flashing(flashMessage)
    }
  }

  def editTask(id: Int) = Action.async {implicit request =>
    taskDAO.findById(id).flatMap {
      case Some(task) =>
        val taskData: TaskData = TaskData(Some(task.id), task.title, task.description,
          new SimpleDateFormat("dd-MM-yyyy").parse(task.dueDate))
        taskDAO.all.map(tasks => Ok(views.html.main(tasks, TaskDAO.createTaskForm.fill(taskData))));
      case None => Future(Redirect(routes.Application.index()).flashing("error" -> s"Could not edit task with id $id"))
    }
  }

  def fileNotFound() = Action {implicit request => NotFound(views.html.notFound())}

  def index() = Action.async {implicit request =>
    taskDAO.all.map(tasks => Ok(views.html.main(tasks, TaskDAO.createTaskForm)))
  }

  def uploadTask() = Action.async { implicit request =>
    def failure(formWithErrors: Form[TaskData]) = {
      taskDAO.all.map(tasks => BadRequest(views.html.main(tasks, formWithErrors)))
    }

    def success(taskData: TaskData) = {
      val writeResult = taskData.id match {
        case Some(id) => taskDAO.update(taskData)
        case None => taskDAO.create(taskData)
      }
      writeResult.map {
        case w: UpdateWriteResult =>
          val flashMessage = if(w.ok) "success" -> "Updated task" else "error" -> "Could not update task"
          Redirect(routes.Application.index()).flashing(flashMessage)
        case w: WriteResult =>
          val flashMessage = if(w.ok) "success" -> "Created task" else "error" -> "Could not create task"
          Redirect(routes.Application.index()).flashing(flashMessage)
        case _ => throw new IllegalArgumentException
      }
    }

    TaskDAO.createTaskForm.bindFromRequest().fold(failure, success)
  }


}
