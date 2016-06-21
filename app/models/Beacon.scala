package models

import java.util.Date

import play.api.libs.iteratee.Iteratee

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This is the class defining Beacons stored in the database
  *
  * @constructor creates a Beacon
  * @param
  */
case class Beacon(id: Option[BSONObjectID], creator: String, title: String,
                  location: Point, startTime: Date, endTime: Date, range: Double,
                  placeName: String, address: String, tags: List[String],
                  notifiedCount: Int, notified: List[String])
