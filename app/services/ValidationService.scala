package services

import models.User
import models.UserData

import javax.inject.Inject

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ValidationService @Inject()(userService: UserService){
  def validate(password: String, passwordConfirmation: String, phoneNumber: String,
        email: String = "", firstName: String = "", lastName: String = "",
        username: String = "") = {
    (password, phoneNumber.matches("\\d{10}")) match {
      case (`passwordConfirmation`, true) => {
        var maybeUser: Option[User] = null
        if (username != null && !username.equals("")) {
          maybeUser = Await.result(userService.findByPhoneNumberOrUsername(phoneNumber, username), Duration.Inf)
        } else {
          maybeUser = Await.result(userService.findByPhoneNumber(phoneNumber), Duration.Inf)
        }

        maybeUser match {
          case None => Some(UserData(password, passwordConfirmation, phoneNumber, email,
                       firstName, lastName, username))
          case _ => None
        }
      }
      case _ =>
        None
    }
  }
}
