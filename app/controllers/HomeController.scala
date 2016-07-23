package controllers

import play.api.Configuration
import play.api.mvc.Action
import play.api.mvc.Controller

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeController @Inject()(config: Configuration) extends Controller {
  def index() = Action {
    Ok(views.html.index("Hello"))
  }
}
