package services

import models.Beacon
import models.Point

import java.util.Date

import javax.inject.Inject
import javax.inject.Singleton

import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/** Provides CRUD operations for the Beacon model
  */
@Singleton
class BeaconService @Inject()(mongo: MongoService) {
  lazy val beacons = Await.result(mongo.beacons, Duration.Inf)

  /** Creates a new Beacon and inserts it into the database. Creator and location
    * are the two required parameters
    *
    * @param creator - the name of the User creating the Beacon (required)
    * @param location - the location of the Beacon being placed (required)
    * @param title - the title of the activity/event (optional)
    * @param address - the street address of the activity/event (optional)
    * @param venueName - the name of the venue hosting the activity/event (optional)
    * @param startTime - the start time of the event as a java.util.Date; defaults
    *                    to the current time (optional)
    * @param endTime - the end time of the event as a java.util.Date; defaults
    *                  to the current time, but should be startTime + 1 hour if
    *                  the user does not specify on input submission (optional)
    * @param notifiedCount - the number of users that have already been notified
    *                        about this event. 1 will be added to this number
    *                        when inserted to include the creator (optional)
    * @param notifiedUsers - a List of users that have been notified about this
    *                        event. The creator will be prepended on insert (optional)
    * @param range - the radius of the circle (in miles) inside which the users
    *                will be notified; should use the default range of 0.1 (optional)
    * @param tags - a List of tags that describe the Beacon so people can search
    *               by types of events (optional)
    * @return A Future of the result of the insert
    */
  def create(creator: String, location: Point, title: String = "", address: String = "",
             venueName: String = "", startTime: Date = new Date, endTime: Date = new Date,
             notifiedCount: Int = 0, notifiedUsers: List[String] = List(),
             range: Double = 0.1, tags: List[String] = List()): Future[WriteResult] = {
    val createdAt = new Date
    val updatedAt = createdAt

    val newBeacon = Beacon(creator, location, createdAt, updatedAt, title, address,
                           venueName, startTime, endTime, notifiedCount + 1,
                           creator :: notifiedUsers, range, tags)

    beacons.insert(newBeacon)
  }

  /** Saves a preconstructed Beacon in the database
    *
    * @param newBeacon - a Beacon object to be inserted into the database
    * @return A Future of the result of the insert
    */
  def create(newBeacon: Beacon): Future[WriteResult] = {
    beacons.insert(newBeacon)
  }

  /** Removes a Beacon from the database
    *
    * @param beacon - the Beacon to be removed
    * @return A Future of the result of the delete
    */
  def delete(beacon: Beacon): Future[WriteResult] = {
    beacons.remove(beacon)
  }

  /** Finds maxLimit nearby Beacons within range miles of location
    *
    * @param latitude
    * @param longitude
    * @param range - the radius of the circle to search in miles
    * @param maxLimit - the maximum number of Beacons to return
    * @return A List of maxlimit or less Beacons nearby
    */
  def findNearbyBeacons(latitude: Double, longitude: Double, range: Double, maxLimit: Int): Future[List[Beacon]] = {
    val geoQuery = BSONDocument("location" -> BSONDocument("$geoWithin" -> BSONDocument(
      "$centerSphere" -> BSONArray(
        BSONArray(longitude, latitude),
        range/3963.2 // converts from miles to radians
      )
    )))

    beacons.find(geoQuery).cursor[Beacon].collect[List](maxLimit)
  }
}
