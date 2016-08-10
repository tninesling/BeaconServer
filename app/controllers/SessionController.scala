package controllers

import helpers.BCryptHelpers
import models.LoginData
import services.UserService
import services.ValidationService

import java.security.SecureRandom

import javax.inject.Inject
import javax.inject.Singleton

import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Cookie

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SessionController @Inject()(val messagesApi: MessagesApi, userService: UserService, validationService: ValidationService) extends Controller with I18nSupport {
  val loginForm: Form[LoginData] = Form(
    mapping(
      "password" -> nonEmptyText(8, Int.MaxValue),
      "phoneNumber" -> nonEmptyText(10,10),
      "remember" -> boolean
    )(LoginData.apply)(LoginData.unapply) verifying ("Failed form constraints",
      fields => fields match { case loginData =>
        validationService.validateLogin(loginData.password,
                                        loginData.phoneNumber,
                                        loginData.remember).isDefined
      }
    )
  )

  def login = Action.async {
    Future {
      Ok(views.html.login(loginForm))
    }
  }

  def loginPost = Action.async { implicit request =>
    Future {
      loginForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, return form with errors
          BadRequest(views.html.login(formWithErrors))
        },
        loginData => {
          // binding success, redirect to home page with proper session and cookie options
          if (loginData.remember) {
            val token: String = newToken
            val tokenDigest: String = BCryptHelpers.digest(token)

            userService.update(loginData.phoneNumber, ("rememberDigest", tokenDigest))

            Redirect(routes.HomeController.index).withSession(
              "user" -> BCryptHelpers.digest(loginData.phoneNumber)
            ).withCookies(Cookie("user", loginData.phoneNumber), Cookie("rememberMe", token))
          } else {
            Redirect(routes.HomeController.index).withSession(
              "user" -> BCryptHelpers.digest(loginData.phoneNumber)
            )
          }
        }
      )
    }
  }

  def logout = Action.async {
    Future {
      // discard session
      Redirect(routes.HomeController.index).withNewSession
    }
  }

  def newToken: String = {
    val srand: SecureRandom = new SecureRandom
    math.abs(srand.nextInt).toString
  }
}
