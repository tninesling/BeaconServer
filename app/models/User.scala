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
  email: String = null,
  firstName: String = null,
  lastName: String = null,
  location: Point = null,
  username: String = null
) {
  /*require(phoneNumber match {
    case r"\d{10}" => true
    case _ => false
  })*/
  require(phoneNumber.trim.matches("\\d{10}"))
}

object User {
  implicit object UserReader extends BSONDocumentReader[User] {
    override def read(doc: BSONDocument): User = {
      val opt: Option[User] = for {
        createdAt <- doc.getAs[Date]("createdAt")
        passwordDigest <- doc.getAs[String]("passwordDigest")
        phoneNumber <- doc.getAs[String]("phoneNumber")
        updatedAt <- doc.getAs[Date]("updatedAt")
        email <- doc.getAs[String]("email")
        firstName <- doc.getAs[String]("firstName")
        lastName <- doc.getAs[String]("lastName")
        location <- doc.getAs[Point]("location")
        username <- doc.getAs[String]("username")
      } yield User(createdAt, passwordDigest, phoneNumber, updatedAt, email,
              firstName, lastName, location, username)

      opt.getOrElse(null)
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
