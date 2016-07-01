package controllers

import javax.inject.Inject

@Singleton
class HomeController @Inject()() extends Controller {
  def index() = Action {
    Ok(views.html.index("Hello"))
  }
}
