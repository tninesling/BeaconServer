package models

import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

/**
  * This case class implements the GeoJSON specification for a Point object
  *
  * @param lat the latitude coordinate of this Point
  * @param lon the longitude coordinate of this Point
  */
case class Point(lat: Double, lon: Double)

object Point {
  implicit object PointWriter extends BSONDocumentWriter[Point] {
    def write(point: Point): BSONDocument = BSONDocument(
      "type" -> "Point",
      "coordinates" -> Seq(point.lon, point.lat)
    )
  }

  implicit object PointReader extends BSONDocumentReader[Point] {
    def read(doc: BSONDocument): Point = {
      Point(
        doc.getAs[Seq[Double]]("coordinates").head.head,
        doc.getAs[Seq[Double]]("coordinates").last.last
      )
    }
  }
}
