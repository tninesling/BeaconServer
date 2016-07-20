package controllers

import models.UserData
import services.MongoService
import services.UserService

import javax.inject.Inject

import play.api.Configuration
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Ok
import play.api.mvc.Results.Redirect

class UserController @Inject()(val messagesApi: MessagesApi, userService: UserService) extends Controller with I18nSupport {
  def validate(password: String, passwordConfirmation: String, phoneNumber: String,
        email: Option[String], firstName: Option[String], lastName: Option[String],
        username: Option[String]) = {
    password match {
      case passwordConfirmation =>
        Some(UserData(password, passwordConfirmation, phoneNumber, email,
        firstName, lastName, username))
    }
  }

  val signUpForm: Form[UserData] = Form(
    mapping(
      "password" -> nonEmptyText(8, Int.MaxValue),
      "passwordConfirmation" -> nonEmptyText(8, Int.MaxValue),
      "phoneNumber" -> nonEmptyText(10, 10),
      "email" -> optional(email),
      "firstName" -> optional(text),
      "lastName" -> optional(text),
      "username" -> optional(text)
    )(UserData.apply)(UserData.unapply) verifying ("Failed form constraints",
      fields => fields match { case userData => validate(userData.password,
        userData.passwordConfirmation, userData.phoneNumber, userData.email,
        userData.firstName, userData.lastName, userData.username
      ).isDefined }
    )
  )

  def userPost = Action { implicit request =>
    signUpForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, form has errors
        BadRequest(views.html.signup(formWithErrors))
      },
      userData => {
        // binding success, form is bound successfully
        //userService.create(userData.phoneNumber, userData.password, userData.email,
        //      userData.firstName, userData.lastName, None, userData.username)
        Redirect(routes.HomeController.index)
      }
    )
  }

  def signup = Action {
    Ok(views.html.signup(signUpForm))
  }
}
