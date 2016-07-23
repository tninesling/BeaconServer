package services

import models.Point
import models.User

import java.util.Date

import javax.inject.Inject
import javax.inject.Singleton

import org.mindrot.jbcrypt.BCrypt

import play.api.Configuration

import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Producer

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/** Provides CRUD operations and authentication for the User model.
  * Authentication is provided by the jBcrypt plugin
  */
@Singleton
class UserService @Inject()(config: Configuration, mongo: MongoService) {
  lazy val users = Await.result(mongo.users, Duration.Inf)

  /** Authenticates a user based on a password attempt. Finds the user trying to
    * authenticate by phone number and then checks the passwordEntry against
    * the salted hash in the found user's passwordDigest field using jBcrypt
    *
    * @param phoneNumber - the user's phoneNumber
    * @param passwordEntry - the user's password attempt
    */
  def authenticate(phoneNumber: String, passwordEntry: String): Boolean = {
    val query: BSONDocument = BSONDocument("phoneNumber" -> phoneNumber)
    val maybeUser: Future[Option[User]] = users.find(query).one[User]
    val user: User = Await.result(maybeUser, Duration.Inf).get

    BCrypt.checkpw(passwordEntry, user.passwordDigest)
  }

  /** If the specified user does not already exist, creates a new user and
    * inserts into the database. Phone number and password are the only
    * required parameters for creating a new user
    *
    * @param phoneNumber - the user's phone number (required)
    * @param password - the user's chosen password, which will be hashed before
    *                   being stored in the database (required)
    * @param email - the user's email address (optional)
    * @param firstName - the user's first name (optional)
    * @param lastName - the user's last name (optional)
    * @param location - the user's location in the form of a Point (optional)
    * @param username - the user's chosen username (optional)
    * @return A Future of the result of the insert
    */
  def create(phoneNumber: String, password: String, email: String = null,
        firstName: String = null, lastName: String = null, location: Point = null,
        username: String = null): Future[WriteResult] = {
    val createdAt = new Date // current time
    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt)
    val updatedAt = new Date // current time
    val newUser: User = User(createdAt, hashedPassword, phoneNumber, updatedAt,
            email, firstName, lastName, location, username)

    users.insert(newUser)
  }

  /** Saves a new User in the database from a preconstructed User object
    *
    * @param newUser - A User object to be inserted into the database
    * @return A Future of the result of the insert
    */
  def create(newUser: User): Future[WriteResult] = {
    users.insert(newUser)
  }

  /** Deletes the specified User from the database
    *
    * @param user - the User to be deleted
    * @return A Future of the result of the delete
    */
  def delete(user: User): Future[WriteResult] = {
    users.remove(user)
  }

  /** Deletes the User with the specified phoneNumber
    *
    * @param phoneNumber - the user's phone number
    */
  def delete(phoneNumber: String): Future[WriteResult] = {
    val user = BSONDocument("phoneNumber" -> phoneNumber)
    users.remove(user)
  }

  /** Find the user inside the database
    *
    * @param user - the User to find in the database
    * @return A Future Option of a User matching user
    */
  def find(user: User): Future[Option[User]] = {
    users.find(user).one[User]
  }

  /** Finds a User in the database with the specified phone number.
    *
    * @param phoneNumber - the user's phoneNumber
    * @return A Future Option of a User with the specified phone number
    */
  def findByPhoneNumber(phoneNumber: String): Future[Option[User]] = {
    val user = BSONDocument("phoneNumber" -> BSONDocument("$eq" -> phoneNumber))
    users.find(user).one[User]
  }

  /** Finds a User in the database with the specified username
    *
    * findByPhoneNumber is more likely to return a result than this function
    * because username is an optional parameter for User creation while
    * phone number is required
    *
    * @param username - the user's username
    * @return A Future Option of a User with the specified username
    */
  def findByUsername(username: String): Future[Option[User]] = {
    val user = BSONDocument("username" -> username)
    users.find(user).one[User]
  }

  /** Finds and updates a user with the specified phone number to have the
    * values specified in updates. The updates specified as tuples of key value
    * pairs must be for String fields in the User model
    *
    * @param phoneNumber - the user's phone number
    * @param updates - a  key value pairs of updates for the user
    * @return A Future FindAndModifyResult for the update issued
    */
  def update(phoneNumber: String, updates: (String, String)*): Future[users.BatchCommands.FindAndModifyCommand.FindAndModifyResult] = {
    val user = BSONDocument("phoneNumber" -> phoneNumber)
    val updatesMap = updates.map {
      update => BSONDocument(update._1 -> update._2)
    }

    val bsonUpdates: BSONDocument = updatesMap.tail.foldLeft(updatesMap.head)(
      (item1: BSONDocument, item2: BSONDocument) => item1.add(item2)
    )

    users.findAndUpdate(user, bsonUpdates)
  }

  /** Finds a user with the specified phone number and updates the user's
    * location to the specified latitude and longitude
    *
    * @param phoneNumber - the user's phone number
    * @param latitude - the user's latitude coordinate
    * @param longitude - the user's longitude coordinate
    * @return A Future FindAndModifyResult for the update issued
    */
  def updateLocation(phoneNumber: String, latitude: Double, longitude: Double): Future[users.BatchCommands.FindAndModifyCommand.FindAndModifyResult] = {
    val user = BSONDocument("phoneNumber" -> phoneNumber)
    val newLocation = Point(latitude, longitude)

    users.findAndUpdate(user, newLocation)
  }
}
