package controllers

import models.Beacon
import play.api.mvc._
import play.api.Configuration
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.mutable.ArrayBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import javax.inject.Inject

class BeaconController @Inject()(config: Configuration) extends Controller {
  // grab host name and db name configurations from application.conf

  // if no host names found, default to localhost:27017
  lazy val hostURI = "mongodb://" + config.getString("mongodb.server")
                                          .getOrElse("localhost:27017")
                                          .toString()

  // if no db name found, default to test
  lazy val dbName = config.getString("mongodb.db")
                          .getOrElse("test")

  def test = Action {
    implicit request =>
    request.session.get("user").map{ user =>
      Ok("What's up, " + user)
    }.getOrElse(
      Ok("Let's just make you a new session").withSession(
        "user" -> "newUser"
      )
    )
  }

  // create new beacon
  // returns 200:Ok response for successful creation
  // returns 400:BadRequest response for invalid coordinates or on insert failure
  def create = Action {
    implicit request =>
      // collect and parse the parameters from the request
      val queries = request.queryString

      val creator: String = queries.get("user") match {
        case Some(seq) => seq.head
        case None => ""
      }
      val title: String = queries.get("title") match {
        case Some(seq) => seq.head
        case None => ""
      }
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
      // if no start time is specified, use the current time
      val startTime: Long = queries.get("start") match {
        case Some(seq) => Try(seq.head.toLong) match {
          case Success(t) => if (t > System.currentTimeMillis) {
                               t
                             } else {
                               System.currentTimeMillis
                             }
          case Failure(_) => System.currentTimeMillis
        }
        case None => System.currentTimeMillis
      }
      // if no end time is specified, default to one hour from start time
      val oneHour = 1000 * 60 * 60
      val endTime: Long = queries.get("end") match {
        case Some(seq) => Try(seq.head.toLong) match {
          case Success(t) => if (t > startTime) {
                               t
                             } else {
                               startTime + oneHour
                             }
          case Failure(_) => startTime + oneHour
        }
        case None => startTime + oneHour
      }
      // range parameter defaults to 0.1
      val range: Double = queries.get("range") match {
        case Some(seq) => Try(seq.head.toDouble) match {
          case Success(t) => t
          case Failure(_) => 0.1
        }
        case None => 0.1
      }
      // Defaults for placeName and address currently set to empty string
      // Should use either Foursquare or Google Places API to get this info from coordinates
      val placeName: String = queries.get("place") match {
        case Some(seq) => seq.head
        case None => ""
      }
      val address: String = queries.get("addr") match {
        case Some(seq) => seq.head
        case None => ""
      }
      val tags = queries.get("tags").getOrElse(new ArrayBuffer[String]).asJava

      // Return appropriate response based on parameters
      if (creator.equals("")) {
        BadRequest("No user specified")
      }
      else if (math.abs(latCoord) > 90 || math.abs(longCoord) > 180) {
        BadRequest("Coordinate error")
      } else {
        val newBeacon = new Beacon(hostURI, dbName, creator, title, latCoord, longCoord,
                                   startTime, endTime, range, placeName, address, tags);
        val insertResult = newBeacon.insert

        newBeacon.closeConnection

        if (!insertResult) {
          Ok("Beacon cannot be inserted")
        } else {
          Ok("Beacon successfully created")
        }
      }
  }

  // locate beacons nearby a specified location
  def near = Action{
    implicit request =>
      val queries = request.queryString

      // both functions to find beacons require latitude and longitude
      // if max and distance are not specified, they will have default vals of 5 and 0.1
      // query should fail if either coordinate is null
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
      val max: Int = queries.get("max") match {
        case Some(seq) => Try(seq.head.toInt) match {
          case Success(t) => t
          case Failure(_) => 5
        }
        case None => 5
      }

      // tags are optional feature, function call will depend on presence of tags
      // tags will be a Seq[String] if present - convert to Java List<String> with asJava
      val tags = queries.get("tags").getOrElse(new ArrayBuffer).asJava

      // query returns bad request response if required fields are null
      if (math.abs(latCoord) > 90 || math.abs(longCoord) > 180) {
        BadRequest("Coordinate error")
      } else {
        val finder = new Beacon(hostURI, dbName)
        var found = ""
        // on good request and null tags, call findNearbyBeacons
        if (tags.isEmpty) {
          found = finder.findNearbyBeacons(max, latCoord, longCoord, distance)
        } else {
          found = finder.findNearbyBeaconsByTags(max, latCoord, longCoord, distance, tags)
        }
        finder.closeConnection
        Ok(found)
      }
  }

  // update field in a Beacon
  // Beacon is specified by username and any time within beacon's lifespan
  def update = Action {
    implicit request =>
      val queries = request.queryString

      // get the user and time from queryString
      // default user to empty string - return BadRequest for no user entry
      val user = queries.get("user") match {
        case Some(seq) => seq.head
        case None => ""
      }
      // time defaults to current time for current beacon lookup
      val time = queries.get("time") match {
        case Some(seq) => Try(seq.head.toLong) match {
          case Success(t) => t
          case Failure(_) => System.currentTimeMillis
        }
        case None => System.currentTimeMillis
      }

      if (user.equals("")) {
        BadRequest("User not specified")
      } else {
        val beaconToUpdate = new Beacon(hostURI, dbName)
        // If beacon not found, respond with 404:Beacon not found
        val beaconFound = beaconToUpdate.findBeacon(user, time)
        if (!beaconFound) {
          beaconToUpdate.closeConnection
          Ok("No Beacon found to update")
        } else {
          val updated = queryMatchUpdate(beaconToUpdate, request.queryString)
          beaconToUpdate.closeConnection

          if (updated) {
            Ok("Update successful")
          } else {
            Ok("Update unsuccessful")
          }
        }
      }
  }

  // calls an update on the specified Beacon for each field in the query string
  def queryMatchUpdate(beaconToUpdate: Beacon, queries: Map[String, Seq[String]]): Boolean = {
    val titleUpdated = queries.get("title") match {
      case Some(seq) => beaconToUpdate.updateTitle(seq.head)
      case None => false
    }
    val locationUpdated = (queries.get("lat"), queries.get("lon")) match {
      case (Some(latSeq), Some(lonSeq)) => Try(beaconToUpdate.updateLocation(latSeq.head.toDouble, lonSeq.head.toDouble)) match {
        case Success(t) => t
        case Failure(_) => false
      }
      case (Some(latSeq), None) => false
      case (None, Some(lonSeq)) => false
      case (None, None) => false
    }
    val startTimeUpdated = queries.get("start") match {
      case Some(seq) => Try(beaconToUpdate.updateStartTime(seq.head.toLong)) match {
        case Success(t) => t
        case Failure(_) => false
      }
      case None => false
    }
    val endTimeUpdated = queries.get("end") match {
      case Some(seq) => Try(beaconToUpdate.updateEndTime(seq.head.toLong)) match {
        case Success(t) => t
        case Failure(_) => false
      }
      case None => false
    }
    val rangeUpdated = queries.get("range") match {
      case Some(seq) => Try(beaconToUpdate.updateRange(seq.head.toDouble)) match {
        case Success(t) => t
        case Failure(_) => false
      }
      case None => false
    }
    val placeNameUpdated = queries.get("place") match {
      case Some(seq) => beaconToUpdate.updatePlaceName(seq.head)
      case None => false
    }
    val addressUpdated = queries.get("addr") match {
      case Some(seq) => beaconToUpdate.updateAddress(seq.head)
      case None => false
    }
    val tagsUpdated = queries.get("tags") match {
      case Some(seq) => beaconToUpdate.updateTags(seq.asJava)
      case None => false
    }

    titleUpdated || locationUpdated || startTimeUpdated || endTimeUpdated || rangeUpdated || placeNameUpdated || addressUpdated || tagsUpdated
  }

  // delete's the user's current or future beacon
  def delete = Action {
    implicit request =>
      val qs = request.queryString
      val beacon = new Beacon(hostURI, dbName)
      val user = qs.get("user") match {
        case Some(seq) => seq.head
        case None => ""
      }
      if (user.equals("")) {
        BadRequest("User not specified")
      } else {
        val deleted = beacon.deleteNextBeacon(user)
        beacon.closeConnection
        if (deleted) {
          Ok("Delete successful")
        } else {
          Ok("Delete unsuccessful")
        }
      }
  }
}
