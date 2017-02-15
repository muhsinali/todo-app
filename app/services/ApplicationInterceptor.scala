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
  *
  * Note: The database is only cleared if the application is running in dev/test mode.
  */
@Singleton
class ApplicationInterceptor @Inject() (reactiveMongoApi: ReactiveMongoApi, env: Environment,
                                        lifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext){

  val taskDAO = new TaskDAO(reactiveMongoApi)
  onStartup()
  if(!env.isProd) lifecycle.addStopHook(() => onShutdown())


  def onStartup(): Unit = {
    Logger.info("Populating database with tasks on startup")
    val file = new File("public/exampleTask.json")
    assert(file.exists() && file.isFile && file.canRead)

    val source = Source.fromFile(file)
    val taskData = Json.parse(source.mkString).as[TaskData]
    source.close()

    taskDAO.create(taskData).onFailure{
      case throwable => Logger.error("Failed to create task object", throwable)
    }

  }

  def onShutdown(): Future[Boolean] = {
    Logger.info("Clearing database on shutdown")
    taskDAO.drop()
  }
}
