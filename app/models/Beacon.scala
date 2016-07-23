package models

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class Beacon(
  address: String,
  creator: String,
  endTime: Long,
  location: Point,
  notifiedCount: Int,
  notitifiedUsers: List[String],
  placeName: String,
  range: Double,
  startTime: Long,
  tags: List[String],
  title: String
)

object Beacon {
  implicit object BeaconReader extends BSONDocumentReader[Beacon] {
    def read(bson: BSONDocument): Beacon = {
      val opt: Option[Beacon] = for {
        address <- bson.getAs[String]("address")
        creator <- bson.getAs[String]("creator")
        endTime <- bson.getAs[Long]("endTime")
        location <- bson.getAs[Point]("location")
        notifiedCount <- bson.getAs[Int]("notifiedCount")
        notifiedUsers <- bson.getAs[List[String]]("notifiedUsers")
        placeName <- bson.getAs[String]("placeName")
        range <- bson.getAs[Double]("range")
        startTime <- bson.getAs[Long]("startTime")
        tags <- bson.getAs[List[String]]("tags")
        title <- bson.getAs[String]("title")
      } yield Beacon(address, creator, endTime, location, notifiedCount,
                     notifiedUsers, placeName, range, startTime, tags, title)

      opt.getOrElse(null)
    }
  }

  implicit object BeaconWriter extends BSONDocumentWriter[Beacon] {
    def write(beacon: Beacon): BSONDocument = BSONDocument(
      "address" -> beacon.address,
      "creator" -> beacon.creator,
      "endTime" -> beacon.endTime,
      "location" -> beacon.location,
      "notifiedCount" -> beacon.notifiedCount,
      "notifiedUsers" -> BSONArray(beacon.notitifiedUsers),
      "placeName" -> beacon.placeName,
      "range" -> beacon.range,
      "startTime" -> beacon.startTime,
      "tags" -> BSONArray(beacon.tags),
      "title" -> beacon.title
    )
  }
}
