package controllers

import play.api.Configuration
import play.api.mvc.Action
import play.api.mvc.Controller

import javax.inject.Inject

class HomeController @Inject()() extends Controller {
  def index() = Action {
    Ok(views.html.index("Hello"))
  }
}
