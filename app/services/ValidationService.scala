package services

import models.UserData

import javax.inject.Inject

class ValidationService @Inject()(userService: UserService){
  def validate(password: String, passwordConfirmation: String, phoneNumber: String,
        email: String = null, firstName: String = null, lastName: String = null,
        username: String = null) = {
    password match {
      case `passwordConfirmation` =>
        Some(UserData(password, passwordConfirmation, phoneNumber, email,
        firstName, lastName, username))
      case _ =>
        None
    }
  }
}
