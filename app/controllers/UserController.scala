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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.text
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Ok
import play.api.mvc.Results.Redirect

import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(val messagesApi: MessagesApi, userService: UserService, validationService: ValidationService) extends Controller with I18nSupport {
  val signUpForm: Form[UserData] = Form(
    mapping(
      "password" -> nonEmptyText(8, Int.MaxValue),
      "passwordConfirmation" -> nonEmptyText(8, Int.MaxValue),
      "phoneNumber" -> nonEmptyText(10, 10),
      "email" -> text,
      "firstName" -> text,
      "lastName" -> text,
      "username" -> text
    )(UserData.apply)(UserData.unapply) verifying ("Failed form constraints",
      fields => fields match { case userData =>
        validationService.validateSignup(userData.password,
              userData.passwordConfirmation, userData.phoneNumber, userData.email,
              userData.firstName, userData.lastName, userData.username
        ).isDefined
      }
    )
  )

  def signup = Action.async {
    Future {
      Ok(views.html.signup(signUpForm))
    }
  }

  def signupPost = Action.async { implicit request =>
    Future {
      signUpForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, return form with errors
          BadRequest(views.html.signup(formWithErrors))
        },
        userData => {
          // binding success, form is bound successfully
          userService.create(userData.phoneNumber, userData.password, userData.email,
                userData.firstName, userData.lastName, new Point(0.0, 0.0), userData.username)
          Redirect(routes.HomeController.index).withSession("loggedIn" -> "true")
        }
      )
    }
  }

  def testFind = Action.async {
    Future {
      //val testUser = User(new Date, "password", "5555555558", new Date, "email@email.com", "taylor", "ninesling", Point(35.0, 45.0), "t9sling")
      //userService.create(testUser)
      userService.create("5555555555", "password", "email@email.com", "first", "last", new Point(0.0, 0.0), "username")
      val user = Await.result(userService.findByPhoneNumber("5555555555"), Duration.Inf)
      Ok(user.getOrElse("No user match").toString)
    }
  }
}
