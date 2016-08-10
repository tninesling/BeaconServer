package models

case class LoginData(password: String, phoneNumber: String, remember: Boolean) {
  require(phoneNumber.matches("\\d{10}"))
}
