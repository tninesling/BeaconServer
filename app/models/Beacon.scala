package models

import java.util.Date

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class Beacon(
  // required
  creator: String,
  location: Point,
  // optional
  createdAt: Date = new Date,
  updatedAt: Date = new Date,
  title: String = "",
  address: String = "",
  venueName: String = "",
  startTime: Date = new Date,
  endTime: Date = new Date,
  notifiedCount: Int = 0,
  notifiedUsers: List[String] = List(),
  range: Double = 0.1, // range in miles
  tags: List[String] = List()
) {
  require(!updatedAt.before(createdAt))
  require(!endTime.before(startTime))
}

object Beacon {
  implicit object BeaconReader extends BSONDocumentReader[Beacon] {
    def read(doc: BSONDocument): Beacon = {
      val opt: Option[Beacon] = for {
        creator <- doc.getAs[String]("creator")
        location <- doc.getAs[Point]("location")
        createdAt <- doc.getAs[Date]("createdAt")
        updatedAt <- doc.getAs[Date]("updatedAt")
        title <- doc.getAs[String]("title")
        address <- doc.getAs[String]("address")
        venueName <- doc.getAs[String]("venueName")
        startTime <- doc.getAs[Date]("startTime")
        endTime <- doc.getAs[Date]("endTime")
        notifiedCount <- doc.getAs[Int]("notifiedCount")
        notifiedUsers <- doc.getAs[List[String]]("notifiedUsers")
        range <- doc.getAs[Double]("range")
        tags <- doc.getAs[List[String]]("tags")
      } yield Beacon(creator, location, createdAt, updatedAt, title, address,
                     venueName, startTime, endTime, notifiedCount, notifiedUsers,
                     range, tags)

      opt.getOrElse(null)
    }
  }

  implicit object BeaconWriter extends BSONDocumentWriter[Beacon] {
    def write(beacon: Beacon): BSONDocument = BSONDocument(
      "creator" -> beacon.creator,
      "location" -> beacon.location,
      "createdAt" -> beacon.createdAt,
      "updatedAt" -> beacon.updatedAt,
      "title" -> beacon.title,
      "address" -> beacon.address,
      "venueName" -> beacon.venueName,
      "startTime" -> beacon.startTime,
      "endTime" -> beacon.endTime,
      "notifiedCount" -> beacon.notifiedCount,
      "notifiedUsers" -> beacon.notifiedUsers,
      "range" -> beacon.range,
      "tags" -> beacon.tags
    )
  }
}
