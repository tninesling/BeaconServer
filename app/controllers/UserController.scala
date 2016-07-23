package controllers

import models.Point
import models.User
import models.UserData
import services.MongoService
import services.UserService
import services.ValidationService

import java.util.Date

import javax.inject.Inject
import javax.inject.Singleton

import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Ok
import play.api.mvc.Results.Redirect

import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(val messagesApi: MessagesApi, userService: UserService) extends Controller with I18nSupport {
  val signUpForm: Form[UserData] = Form(
    mapping(
      "password" -> nonEmptyText(8, Int.MaxValue),
      "passwordConfirmation" -> nonEmptyText(8, Int.MaxValue),
      "phoneNumber" -> nonEmptyText(10, 10),
      "email" -> text,//email,
      "firstName" -> text,
      "lastName" -> text,
      "username" -> text
    )(UserData.apply)(UserData.unapply) verifying ("Failed form constraints",
      fields => fields match { case userData => ValidationService.validate(userData.password,
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
        userService.create(userData.phoneNumber, userData.password, userData.email,
              userData.firstName, userData.lastName, null, userData.username)
        Redirect(routes.HomeController.index).flashing("success" -> "User created")
      }
    )
  }

  def signup = Action {
    Ok(views.html.signup(signUpForm))
  }

  def testFind = Action {
    //val testUser = User(new Date, "password", "5555555555", new Date, "email@email.com", "taylor", "ninesling", Point(35.0, 45.0), "t9sling")
    //userService.create(testUser)
    val user = Await.result(userService.findByPhoneNumber("5555555556"), Duration.Inf)
    Ok(user.getOrElse("No user match").toString)
  }

  def specTest = true
}
