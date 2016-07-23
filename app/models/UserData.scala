package models

import java.util.Date

case class UserData(
  // required
  password: String,
  passwordConfirmation: String,
  phoneNumber: String,
  // optional
  email: String = "",
  firstName: String = "",
  lastName: String = "",
  username: String = ""
)
