package controllers

import helpers.JsonHelpers.userToJson
import models.Beacon
import models.BeaconUser
import play.api.libs.json._
import play.api.mvc._
import play.api.Configuration
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable.ArrayBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import javax.inject.Inject

class BeaconUserController @Inject()(config: Configuration) extends Controller {
  // grab host name and db name configurations from application.conf

  // if no host names found, default to localhost:27017
  lazy val hostURI = "mongodb://" + config.getString("mongodb.server")
                                          .getOrElse("localhost:27017")
                                          .toString()

  // if no db name found, default to test
  lazy val dbName = config.getString("mongodb.db")
                          .getOrElse("test")

  def create = Action {
    implicit request =>
      val queries = request.queryString

      val username: String = queries.get("name") match {
        case Some(seq) => seq.head
        case None => ""
      }
      val password: String = queries.get("pass") match {
        case Some(seq) => seq.head
        case None => ""
      }
      val interests = queries.get("interests")
                             .getOrElse(new ArrayBuffer[String])
                             .asJava
      val latCoord: Double = queries.get("lat") match {
       case Some(seq) => Try(seq.head.toDouble) match {
         case Success(t) => t
         case Failure(_) => 999
       }
       case None => 999 // represents null value, check later and return BadRequest
      }
      val longCoord: Double = queries.get("lon") match {
       case Some(seq) => Try(seq.head.toDouble) match {
         case Success(t) => t
         case Failure(_) => 999
       }
       case None => 999 // represents null value, check later and return BadRequest
      }

      if (username.equals("")) {
        BadRequest("Username required")
      } else if (password.equals("")) {
        BadRequest("Password required")
      } else if (math.abs(latCoord) > 90 || math.abs(longCoord) > 180) {
        BadRequest("Coordinate error")
      } else {
        val newUser = new BeaconUser(hostURI, dbName, username, password, interests, latCoord, longCoord)

        val insertResult = newUser.insert

        newUser.closeConnection

        if (!insertResult) {
          Ok("User cannot be inserted")
        } else {
          Ok("User successfully inserted")
        }
      }
  }

  def near = Action {
    implicit request =>
      val queries = request.queryString

      val latCoord: Double = queries.get("lat") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 999
        }
        case None => 999 // 999 represents null value
      }
      val longCoord: Double = queries.get("lon") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 999
        }
        case None => 999 // 999 represents null value
      }
      val distance: Double = queries.get("dist") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 0.1
        }
        case None => 0.1
      }
      val selected = queries.get("sel")
                            .getOrElse(new ArrayBuffer[String])
                            .asJava
      val notified = queries.get("not")
                            .getOrElse(new ArrayBuffer[String])
                            .asJava

      // query returns bad request response if required fields are null
      if (math.abs(latCoord) > 90 || math.abs(longCoord) > 180) {
        BadRequest("Coordinate error")
      } else {
        val finder = new BeaconUser(hostURI, dbName)
        var found = ""

        if (selected.isEmpty) {
          found = finder.findNearbyUsers(latCoord, longCoord, distance, notified)
        } else {
          found = finder.privateFindNearbyUsers(latCoord, longCoord, distance, notified, selected)
        }

        finder.closeConnection
        Ok(found)
      }
  }

  def find = Action {
    implicit request =>
    val queries = request.queryString

    var user = new BeaconUser(hostURI, dbName)

    val name: String = queries.get("name") match {
      case Some(seq) => seq.head
      case None => ""
    }

    user.getUserByName(name)
    user.closeConnection()
    Ok(userToJson(user))
  }

  // successful location update triggers search for nearby beacons
  def updateLoc = Action {
    implicit request =>
      val queries = request.queryString

      val userToUpdate = new BeaconUser(hostURI, dbName)

      val user: String = queries.get("user") match {
        case Some(seq) => seq.head
        case None => ""
      }

      val latCoord: Double = queries.get("lat") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 999
        }
        case None => 999 // 999 represents null value
      }
      val longCoord: Double = queries.get("lon") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 999
        }
        case None => 999 // 999 represents null value
      }

      val locationUpdated = (!user.equals("") && !(math.abs(latCoord) > 90) && !(math.abs(longCoord) > 180) &&
                             userToUpdate.updateLastLocation(user, latCoord, longCoord))

      userToUpdate.closeConnection

      if (!locationUpdated) {
        Ok("Location not updated")
      } else {
        // trigger nearby user notification function
        //notifyNearbyUsers(latCoord, longCoord)
        val finder = new Beacon(hostURI, dbName)
        val beaconList = finder.findNearbyBeaconsWithoutUser(100, latCoord, longCoord, 0.1, user)
        finder.closeConnection
        if (beaconList.equals("{\"beacons\": []}")) {
          Ok("false")
        } else {
          Ok("true")
        }
      }
  }

  /*// finds 10 beacons within 1 mile of the user's location
  def notifyNearbyUsers(latCoord: Double, longCoord: Double) = {
    val beaconFinder = new Beacon(hostURI, dbName)
    val nearBeacons = Json.parse(beaconFinder.findNearbyBeacons(10, latCoord, longCoord, 1))
  }*/
}
