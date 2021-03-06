package models

import reactivemongo.bson.BSONArray
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

case class Point(lat: Double, lon: Double) {
  require(lat >= -90.0)
  require(lat <= 90.0)
  require(lon >= -180.0)
  require(lon <= 180.0)
}

object Point {
  implicit object PointReader extends BSONDocumentReader[Point] {
    def read(bson: BSONDocument): Point = {
      val opt: Option[Point] = for {
        coords <- bson.getAs[BSONArray]("coordinates")
        lon <- coords.getAs[Double](0)
        lat <- coords.getAs[Double](1)
      } yield new Point(lat, lon)

      opt.getOrElse(null)
    }
  }

  implicit object PointWriter extends BSONDocumentWriter[Point] {
    def write(point: Point): BSONDocument = BSONDocument(
      "type" -> "Point",
      "coordinates" -> BSONArray(point.lon, point.lat)
    )
  }
}
