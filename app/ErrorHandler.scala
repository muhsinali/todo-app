import javax.inject.{Inject, Singleton}

import controllers.routes
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Used to provide custom implementations for handling client errors.
  */
@Singleton
class ErrorHandler @Inject()(implicit ec: ExecutionContext) extends HttpErrorHandler {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    if(statusCode == play.api.http.Status.NOT_FOUND) {
      return Future(Redirect(routes.Application.fileNotFound()))
    }
    Future(Status(statusCode)("A client error occurred: " + message))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future(InternalServerError("A server error occurred: " + exception.getMessage))
  }
}
