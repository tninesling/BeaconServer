package services

import models.LoginData
import models.User
import models.UserData

import javax.inject.Inject

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ValidationService @Inject()(userService: UserService){
  /** Validates the submission of the User signup form. password, passwordConfirmation,
    * and phoneNumber are the only required parameters. All other parameters
    * have default values.
    *
    * Validates that password and passwordConfirmation are equal, that the
    * given phoneNumber is formatted at 10 consecutive digits, and that there
    * is no existing user with the specified phone number or username
    *
    * @param password
    * @param passwordConfirmation
    * @param phoneNumber
    * @param email
    * @param firstName
    * @param lastName
    * @param username
    * @return An optional instance of UserData
    */
  def validateSignup(password: String, passwordConfirmation: String, phoneNumber: String,
        email: String = "", firstName: String = "", lastName: String = "",
        username: String = ""): Option[UserData] = {
    (password, phoneNumber.matches("\\d{10}")) match {
      case (`passwordConfirmation`, true) => {
        var maybeUser: Option[User] = null
        if (username != null && !username.equals("")) {
          maybeUser = Await.result(userService.findByPhoneNumberOrUsername(phoneNumber, username), Duration.Inf)
        } else {
          maybeUser = Await.result(userService.findByPhoneNumber(phoneNumber), Duration.Inf)
        }

        maybeUser match {
          case None =>
            Some(UserData(password, passwordConfirmation, phoneNumber, email,
                 firstName, lastName, username))
          case _ =>
            None
        }
      }
      case _ =>
        None
    }
  }

  /** Validates the submission of the User login form.
    *
    * Validates that the phone number is formatted at 10 consecutive digits,
    * then calls the userService authenticate method to verify that the user
    * with the given phone number has that password
    *
    * @param password
    * @param phoneNumber
    * @return An optional instance of LoginData
    */
  def validateLogin(password: String, phoneNumber: String): Option[LoginData] = {
    phoneNumber.matches("\\d{10}") match {
      case true =>
        userService.authenticate(phoneNumber, password) match {
          case true =>
            Some(LoginData(password, phoneNumber))
          case false =>
            None
        }
      case false =>
        None
    }
  }
}
