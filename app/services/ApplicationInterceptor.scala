package services

import java.io.File
import javax.inject.{Inject, Singleton}

import controllers.TaskDAO
import models.TaskData
import play.Environment
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

/**
  * Responsible for setting up/tearing down the application.
  */
@Singleton
class ApplicationInterceptor @Inject() (reactiveMongoApi: ReactiveMongoApi, env: Environment,
                                        lifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext){

  val taskDAO = new TaskDAO(reactiveMongoApi)
  onStartup()
  lifecycle.addStopHook(() => onShutdown())


  def onStartup(): Unit = {
    Logger.info("Populating database with tasks on startup")
    val file = new File("public/exampleTasks.json")
    assert(file.exists() && file.isFile && file.canRead)

    val source = Source.fromFile(file)
    val taskDataList = (Json.parse(source.mkString) \ "tasks").as[List[TaskData]]
    taskDataList.foreach{ taskData =>
      taskDAO.create(taskData).onFailure{case t => Logger.error("Failed to create task object", t)}
    }
    source.close()
  }

  def onShutdown(): Future[Boolean] = {
    Logger.info("Clearing database on shutdown")
    taskDAO.drop()
  }
}
