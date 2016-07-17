package models

import java.util.Date

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

import scala.util.matching.Regex

case class User(
  // required
  createdAt: Date,
  passwordDigest: String,
  phoneNumber: String,
  updatedAt: Date,
  // optional
  email: Option[String] = None,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  location: Option[Point] = None,
  username: Option[String] = None
) {
  /*val phoneNumberAcceptPattern = """\d{10}""".r
  require(phoneNumber match {
    case phoneNumberAcceptPattern => true
    case _ => false
  })*/
  require(phoneNumber.matches("\\d{10}"))
}

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
