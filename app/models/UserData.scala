package models

import java.util.Date

case class UserData(
  // required
  password: String,
  passwordConfirmation: String,
  phoneNumber: String,
  // optional
  email: Option[String] = None,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  username: Option[String] = None
)
