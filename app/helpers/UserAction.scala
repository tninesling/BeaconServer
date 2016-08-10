package helpers

import play.api.mvc.ActionBuilder
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    val maybeUser: Option[String] = request.session.get("user")
    maybeUser match {
      case Some(user) =>
        block(request)
      case None => Future {
        Redirect("/login")
      }
    }
  }
}
