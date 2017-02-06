package controllers

import javax.inject.Inject

import models.TaskData
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class TaskDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends Controller
    with MongoController with ReactiveMongoComponents {



  def tasksFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("tasks"))
}


object TaskDAO {
  val createTaskForm = Form(
    mapping(
      "id" -> optional(number),
      "dueDate" -> date,
      "description" -> nonEmptyText
    )(TaskData.apply)(TaskData.unapply)
  )
}