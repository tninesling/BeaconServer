package controllers

import models.LoginData
import services.UserService
import services.ValidationService

import javax.inject.Inject
import javax.inject.Singleton

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.mvc.Controller

@Singleton
class SessionController @Inject()(userService: UserService, validationService: ValidationService) extends Controller {
  val loginForm: Form[LoginData] = Form(
    mapping(
      "password" -> nonEmptyText(8, Int.MaxValue),
      "phoneNumber" -> nonEmptyText(10,10)
    )(LoginData.apply)(LoginData.unapply) verifying ("Failed form constraints",
      fields => fields match { case loginData =>
        validationService.validateLogin(loginData.password, loginData.phoneNumber).isDefined
      }
    )
  )
}
