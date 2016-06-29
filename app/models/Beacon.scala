package models

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

import java.util.Date

case class Beacon(
  creator: String,
  title: String,
  location: Point,
  startTime: Long,
  endTime: Long,
  range: Double,
  placeName: String,
  address: String,
  tags: List[String],
  notifiedCount: Int,
  notitifiedUsers: List[String]
)

object Beacon {
  implicit object BeaconReader extends BSONDocumentReader[Beacon] {
    def read(bson: BSONDocument): Beacon = {
      val opt: Option[Beacon] = for {
        creator <- bson.getAs[String]("creator")
        title <- bson.getAs[String]("title")
        location <- bson.getAs[Point]("location")
        startTime <- bson.getAs[Long]("startTime")
        endTime <- bson.getAs[Long]("endTime")
        range <- bson.getAs[Double]("range")
        placeName <- bson.getAs[String]("placeName")
        address <- bson.getAs[String]("address")
        tags <- bson.getAs[List[String]]("tags")
        notifiedCount <- bson.getAs[Int]("notifiedCount")
        notifiedUsers <- bson.getAs[List[String]]("notifiedUsers")
      } yield Beacon(creator, title, location, startTime, endTime, range,
                     placeName, address, tags, notifiedCount, notifiedUsers)

      opt.get
    }
  }

  implicit object BeaconWriter extends BSONDocumentWriter[Beacon] {
    def write(beacon: Beacon): BSONDocument = BSONDocument(
      "creator" -> beacon.creator,
      "title" -> beacon.title,
      "location" -> beacon.location,
      "startTime" -> beacon.startTime,
      "endTime" -> beacon.endTime,
      "range" -> beacon.range,
      "placeName" -> beacon.placeName,
      "address" -> beacon.address,
      "tags" -> BSONArray(beacon.tags),
      "notifiedCount" -> beacon.notifiedCount,
      "notifiedUsers" -> BSONArray(beacon.notitifiedUsers)
    )
  }
}
