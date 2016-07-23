package models

import java.util.Date

case class UserData(
  // required
  password: String,
  passwordConfirmation: String,
  phoneNumber: String,
  // optional
  email: String = null,
  firstName: String = null,
  lastName: String = null,
  username: String = null
)
