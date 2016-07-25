package models

case class LoginData(password: String, phoneNumber: String) {
  require(phoneNumber.matches("\\d{10}"))
}
