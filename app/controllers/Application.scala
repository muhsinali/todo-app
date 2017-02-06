package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
  * Created by Muhsin Ali on 06/02/2017.
  */
class Application extends Controller {
  import scala.concurrent.ExecutionContext.Implicits.global

  def index() = Action.async {implicit request =>
    Future(Ok("This is the homepage"))
  }



}
