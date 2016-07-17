package models

import java.util.Date

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class User(
  // required
  createdAt: Date,
  passwordDigest: String,
  phoneNumber: String,
  updatedAt: Date,
  // optional
  email: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  location: Option[Point],
  username: Option[String]
)

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      val opt: Option[User] = for {
        createdAt <- doc.getAs[Date]("createdAt")
        passwordDigest <- doc.getAs[String]("passwordDigest")
        phoneNumber <- doc.getAs[String]("phoneNumber")
        updatedAt <- doc.getAs[Date]("updatedAt")
        email <- doc.getAs[String]("email").map(Option(_))
        firstName <- doc.getAs[String]("firstName").map(Option(_))
        lastName <- doc.getAs[String]("lastName").map(Option(_))
        location <- doc.getAs[Point]("location").map(Option(_))
        username <- doc.getAs[String]("username").map(Option(_))
      } yield User(createdAt, passwordDigest, phoneNumber, updatedAt, email,
              firstName, lastName, location, username)

      opt.get
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "createdAt" -> user.createdAt,
      "passwordDigest" -> user.passwordDigest,
      "phoneNumber" -> user.phoneNumber,
      "updatedAt" -> user.updatedAt,
      "email" -> user.email,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "location" -> user.location,
      "username" -> user.username
    )
  }
}
