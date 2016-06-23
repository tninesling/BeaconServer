package controllers

import play.api.mvc._

class WebHomeController extends Controller {
  def index = Action {
    Ok("Welcome to the Home Page")
  }
}
