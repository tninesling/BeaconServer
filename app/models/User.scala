package models

import java.util.Date

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

import scala.util.matching.Regex

case class User(
  // required
  phoneNumber: String,
  passwordDigest: String,
  createdAt: Date,
  updatedAt: Date,
  // optional
  email: String = "",
  firstName: String = "",
  lastName: String = "",
  location: Point = Point(0.0, 0.0),
  username: String = "",
  rememberDigest: String = ""
) {
  require(phoneNumber.matches("\\d{10}"))
}

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    override def read(doc: BSONDocument): User = {
      val opt: Option[User] = for {
        passwordDigest <- doc.getAs[String]("passwordDigest")
        phoneNumber <- doc.getAs[String]("phoneNumber")
        createdAt <- doc.getAs[Date]("createdAt")
        updatedAt <- doc.getAs[Date]("updatedAt")
        email <- doc.getAs[String]("email")
        firstName <- doc.getAs[String]("firstName")
        lastName <- doc.getAs[String]("lastName")
        location <- doc.getAs[Point]("location")
        username <- doc.getAs[String]("username")
        rememberDigest <- doc.getAs[String]("rememberDigest")
      } yield User(phoneNumber, passwordDigest, createdAt, updatedAt, email,
              firstName, lastName, location, username, rememberDigest)

      opt.getOrElse(null)
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "phoneNumber" -> user.phoneNumber,
      "passwordDigest" -> user.passwordDigest,
      "createdAt" -> user.createdAt,
      "updatedAt" -> user.updatedAt,
      "email" -> user.email,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "location" -> user.location,
      "username" -> user.username,
      "rememberDigest" -> user.rememberDigest
    )
  }
}
