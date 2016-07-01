package models

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class User(
  email: String,
  firstName: String,
  lastName: String,
  location: Point,
  password: String,
  phoneNumber: String,
  username: String
)

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    def read(bson: BSONDocument): User = {
      val opt: Option[User] = for {
        email <- bson.getAs[String]("email")
        firstName <- bson.getAs[String]("firstName")
        lastName <- bson.getAs[String]("lastName")
        location <- bson.getAs[Point]("location")
        password <- bson.getAs[String]("password")
        phoneNumber <- bson.getAs[String]("phoneNumber")
        username <- bson.getAs[String]("username")
      } yield User(email, firstName, lastName, location, password, phoneNumber,
                   username)

      opt.get
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "email" -> user.email,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "location" -> user.location,
      "password" -> user.password,
      "phoneNumber" -> user.phoneNumber,
      "username" -> user.username
    )
  }
}
