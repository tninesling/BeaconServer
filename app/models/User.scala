package com.beacon.models

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class User(
  // required
  passwordDigest: String,
  phoneNumber: String,
  // optional
  email: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  location: Option[Point],
  username: Option[String]
)

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    def read(bson: BSONDocument): User = {
      val opt: Option[User] = for {
        passwordDigest <- bson.getAs[String]("passwordDigest")
        phoneNumber <- bson.getAs[String]("phoneNumber")
        email <- bson.getAs[String]("email").map(Option(_))
        firstName <- bson.getAs[String]("firstName").map(Option(_))
        lastName <- bson.getAs[String]("lastName").map(Option(_))
        location <- bson.getAs[Point]("location").map(Option(_))
        username <- bson.getAs[String]("username").map(Option(_))
      } yield User(passwordDigest, phoneNumber, email, firstName, lastName,
              location, username)

      opt.get
    }
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "passwordDigest" -> user.passwordDigest,
      "phoneNumber" -> user.phoneNumber,
      "email" -> user.email,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "location" -> user.location,
      "username" -> user.username
    )
  }
}
